from __future__ import annotations

import argparse
import logging
import os
import re
import shutil
import subprocess
import sys
import tempfile
from dataclasses import dataclass
from math import gcd
from pathlib import Path

import numpy as np
import scipy.signal
import soundfile as sf
import torch
from deep_translator import GoogleTranslator
from faster_whisper import WhisperModel
from TTS.api import TTS


_original_torch_load = torch.load
torch.load = lambda *args, **kwargs: _original_torch_load(
    *args, **{**kwargs, "weights_only": False}
)

os.environ.setdefault("COQUI_TOS_AGREED", "1")
os.environ.setdefault("KMP_DUPLICATE_LIB_OK", "TRUE")

logger = logging.getLogger("dubbing")

DEFAULT_WHISPER_MODEL = "medium"
DEFAULT_BEAM_SIZE = 1
DEFAULT_MAX_BATCH_CHARS = 330
DEFAULT_MAX_BATCH_DURATION = 14.0
DEFAULT_BATCH_END_PAD = 0.18
DEFAULT_MAX_GAP = 1
DEFAULT_TTS_TEMPERATURE = 0.7
DEFAULT_TTS_MAX_CHARS = 180
DEFAULT_BG_GAIN = 0.82
DEFAULT_VOICE_GAIN = 0.95
DEFAULT_MIN_TEMPO = 0.88
DEFAULT_MAX_TEMPO = 1.15
DEFAULT_STRETCH_TOLERANCE = 0.08
DEFAULT_XTTS_MODEL = "tts_models/multilingual/multi-dataset/xtts_v2"


@dataclass(slots=True)
class Segment:
    start: float
    end: float
    text: str


@dataclass(slots=True)
class Batch:
    start: float
    end: float
    segments: list[Segment]

    @property
    def text(self) -> str:
        return normalize_text(" ".join(segment.text for segment in self.segments))


def normalize_text(text: str) -> str:
    return re.sub(r"\s+", " ", text).strip() if text else ""


def clamp(value: float, low: float, high: float) -> float:
    return max(low, min(high, value))


def split_text_for_tts(text: str, max_chars: int = DEFAULT_TTS_MAX_CHARS) -> list[str]:
    text = normalize_text(text)
    if not text:
        return []

    chunks: list[str] = []
    current = ""

    def flush() -> None:
        nonlocal current
        if current:
            chunks.append(current)
            current = ""

    for sentence in re.split(r"(?<=[.!?аҐ¤])\s+", text):
        sentence = normalize_text(sentence)
        if not sentence:
            continue

        if len(sentence) <= max_chars:
            if current and len(current) + len(sentence) + 1 <= max_chars:
                current = f"{current} {sentence}"
            else:
                flush()
                current = sentence
            continue

        flush()
        for part in re.split(r"(?<=[,;:])\s+", sentence):
            part = normalize_text(part)
            if not part:
                continue

            if len(part) <= max_chars:
                if current and len(current) + len(part) + 1 <= max_chars:
                    current = f"{current} {part}"
                else:
                    flush()
                    current = part
            else:
                flush()
                for i in range(0, len(part), max_chars):
                    chunk = part[i : i + max_chars].strip()
                    if chunk:
                        chunks.append(chunk)

    flush()
    return chunks


def build_batches(segments: list[Segment], max_batch_chars: int = DEFAULT_MAX_BATCH_CHARS,
        max_batch_duration: float = DEFAULT_MAX_BATCH_DURATION, max_gap: float = DEFAULT_MAX_GAP) -> list[Batch]:

    batches: list[Batch] = []
    current: list[Segment] = []

    def flush() -> None:
        nonlocal current
        if current:
            batches.append(
                Batch(
                    start=current[0].start,
                    end=current[-1].end + DEFAULT_BATCH_END_PAD,
                    segments=current,
                )
            )
            current = []

    for segment in segments:
        text = normalize_text(segment.text)
        if not text:
            continue

        candidate = current + [Segment(segment.start, segment.end, text)]

        if not current:
            current = candidate
            continue

        candidate_duration = candidate[-1].end - candidate[0].start
        candidate_chars = len(normalize_text(" ".join(item.text for item in candidate)))
        gap = segment.start - current[-1].end

        if (
                gap <= max_gap
                and candidate_duration <= max_batch_duration
                and candidate_chars <= max_batch_chars
        ):
            current = candidate
        else:
            flush()
            current = [Segment(segment.start, segment.end, text)]

    flush()
    return batches


def translate_text(translator: GoogleTranslator, text: str) -> str:
    text = normalize_text(text)
    if not text:
        return ""

    try:
        translated = normalize_text(translator.translate(text))
        return translated or text

    except Exception:
        return text


def get_xtts_output_sample_rate(tts: TTS) -> int:
    candidates = [
        ("synthesizer", "output_sample_rate"),
        ("synthesizer", "sample_rate"),
        ("synthesizer", "tts_model", "ap", "sample_rate"),
        ("synthesizer", "tts_model", "audio_config", "sample_rate"),
    ]

    for chain in candidates:
        obj = tts
        for attr in chain:
            if not hasattr(obj, attr):
                break
            obj = getattr(obj, attr)
        else:
            try:
                sr = int(obj)
                if sr > 0:
                    return sr
            except Exception:
                pass

    return 24000


def resample_audio(audio: np.ndarray, orig_sr: int, target_sr: int) -> np.ndarray:
    audio = np.asarray(audio, dtype=np.float32)
    if audio.size == 0 or orig_sr <= 0 or target_sr <= 0 or orig_sr == target_sr:
        return audio.astype(np.float32)

    ratio = gcd(orig_sr, target_sr)
    up = target_sr // ratio
    down = orig_sr // ratio

    try:
        resampled = scipy.signal.resample_poly(audio, up, down)
        return np.asarray(resampled, dtype=np.float32)

    except Exception:
        new_len = max(1, int(round(len(audio) * (target_sr / orig_sr))))
        try:
            resampled = scipy.signal.resample(audio, new_len)
            return np.asarray(resampled, dtype=np.float32)

        except Exception:
            return audio.astype(np.float32)


def build_atempo_filter(factor: float) -> str:
    if factor <= 0:
        return "atempo=1.0"

    parts: list[float] = []

    while factor > 2.0:
        parts.append(2.0)
        factor /= 2.0

    while factor < 0.5:
        parts.append(0.5)
        factor /= 0.5

    parts.append(factor)
    return ",".join(f"atempo={part:.6f}" for part in parts)


def time_stretch_audio_preserve_pitch( audio: np.ndarray, sr: int, target_seconds: float, ) -> np.ndarray:
    audio = np.asarray(audio, dtype=np.float32)
    if audio.size == 0 or sr <= 0 or target_seconds <= 0:
        return audio

    current_seconds = len(audio) / float(sr)
    if current_seconds <= 0:
        return audio

    factor = current_seconds / float(target_seconds)
    if abs(factor - 1.0) < 0.03:
        return audio

    atempo_filter = build_atempo_filter(factor)

    with tempfile.TemporaryDirectory() as tmpdir:
        tmpdir_path = Path(tmpdir)
        in_path = tmpdir_path / "in.wav"
        out_path = tmpdir_path / "out.wav"

        sf.write(in_path, audio, sr)

        cmd = ["ffmpeg", "-y", "-hide_banner", "-loglevel", "error", "-i", str(in_path), "-filter:a", atempo_filter, str(out_path), ]

        try:
            subprocess.run(cmd, check=True)

            stretched, out_sr = sf.read(out_path, dtype="float32")

            if stretched.ndim > 1:
                stretched = np.mean(stretched, axis=1).astype(np.float32)

            if out_sr != sr:
                stretched = resample_audio(stretched, out_sr, sr)

            return np.asarray(stretched, dtype=np.float32)

        except Exception as exc:
            logger.warning("Error while time-stretch: %s", exc)
            return audio


def synthesize_xtts_batch(tts: TTS, text: str, lang: str, gpt_cond_latent, speaker_embedding, temperature: float, max_tts_chars: int, ) -> np.ndarray:
    chunks = split_text_for_tts(text, max_chars=max_tts_chars)

    if not chunks:
        return np.zeros(0, dtype=np.float32)

    wavs: list[np.ndarray] = []

    for chunk in chunks:
        try:
            result = tts.synthesizer.tts_model.inference(
                text=chunk,
                language=lang,
                gpt_cond_latent=gpt_cond_latent,
                speaker_embedding=speaker_embedding,
                temperature=temperature,
            )
            wav = np.asarray(result["wav"], dtype=np.float32)
            if wav.size:
                wavs.append(wav)

        except Exception as exc:
            logger.warning("Error while synthesize: %s", exc)

    if not wavs:
        return np.zeros(0, dtype=np.float32)

    return np.concatenate(wavs).astype(np.float32)


def fit_audio_to_length(audio: np.ndarray, target_len: int) -> np.ndarray:
    audio = np.asarray(audio, dtype=np.float32)

    if target_len <= 0:
        return np.zeros(0, dtype=np.float32)
    if audio.size == 0:
        return np.zeros(target_len, dtype=np.float32)
    if len(audio) > target_len:
        return audio[:target_len].astype(np.float32)
    if len(audio) < target_len:
        return np.pad(audio, (0, target_len - len(audio))).astype(np.float32)

    return audio.astype(np.float32)


def soft_fit_audio_to_length( audio: np.ndarray, sr: int, target_len: int, min_tempo: float, max_tempo: float, stretch_tolerance: float,) -> np.ndarray:
    audio = np.asarray(audio, dtype=np.float32)

    if target_len <= 0:
        return np.zeros(0, dtype=np.float32)
    if audio.size == 0:
        return np.zeros(target_len, dtype=np.float32)
    if sr <= 0:
        return fit_audio_to_length(audio, target_len)

    current_seconds = len(audio) / float(sr)
    target_seconds = target_len / float(sr)

    if target_seconds <= 0:
        return fit_audio_to_length(audio, target_len)

    ratio = current_seconds / target_seconds

    if abs(ratio - 1.0) > stretch_tolerance:
        safe_factor = clamp(ratio, min_tempo, max_tempo)
        audio = time_stretch_audio_preserve_pitch(
            audio=audio,
            sr=sr,
            target_seconds=current_seconds / safe_factor,
        )

    return fit_audio_to_length(audio, target_len)


def mix_voice_on_background(background: np.ndarray, voice: np.ndarray, start_idx: int, end_idx: int, bg_gain: float, voice_gain: float,) -> None:
    if end_idx <= start_idx:
        return

    region = background[start_idx:end_idx].astype(np.float32, copy=False)
    voice = np.asarray(voice, dtype=np.float32)

    if len(voice) != len(region):
        voice = fit_audio_to_length(voice, len(region))

    background[start_idx:end_idx] = region * bg_gain + voice * voice_gain


def load_mono_audio(path: str) -> tuple[np.ndarray, int]:
    audio, sr = sf.read(path, dtype="float32")
    if audio.ndim > 1:
        audio = np.mean(audio, axis=1).astype(np.float32)

    return np.asarray(audio, dtype=np.float32), int(sr)


def run_demucs(input_audio: Path, output_dir: Path) -> None:
    subprocess.run(
        [sys.executable, "-m", "demucs.separate", "-n", "htdemucs", "--two-stems=vocals", "-o", str(output_dir), str(input_audio)], check=True
    )


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser()
    parser.add_argument("--input", required=True)
    parser.add_argument("--lang", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--workdir", required=True)
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    logging.basicConfig(level=logging.INFO, format="%(message)s")

    device = "cuda" if torch.cuda.is_available() else "cpu"
    compute_type = "float16" if device == "cuda" else "int8"

    input_audio = Path(args.input).resolve()
    output_audio = Path(args.output).resolve()
    workdir = Path(args.workdir).resolve()
    temp_dir = workdir / f"dub_temp_{args.lang}"
    temp_dir.mkdir(parents=True, exist_ok=True)

    try:
        demucs_out = temp_dir / "demucs"
        # 1. dividing vocals and no vocals
        run_demucs(input_audio=input_audio, output_dir=demucs_out)

        base_name = input_audio.stem
        vocals_path = demucs_out / "htdemucs" / base_name / "vocals.wav"
        bg_path = demucs_out / "htdemucs" / base_name / "no_vocals.wav"

        whisper = WhisperModel(DEFAULT_WHISPER_MODEL, device=device, compute_type=compute_type)

        tts = TTS(DEFAULT_XTTS_MODEL).to(device)

        xtts_sr = get_xtts_output_sample_rate(tts)

        #2. copy voice
        gpt_cond_latent, speaker_embedding = tts.synthesizer.tts_model.get_conditioning_latents(
            audio_path=[str(vocals_path)]
        )
        # 3. getting text+timecodes from vocal
        segments_iter, _ = whisper.transcribe(str(vocals_path), beam_size=DEFAULT_BEAM_SIZE, word_timestamps=False, vad_filter=True)

        segments: list[Segment] = []
        for item in segments_iter:
            # 4. clean the text from mess
            text = normalize_text(item.text)
            if text:
                segments.append(
                    Segment(start=float(item.start), end=float(item.end), text=text)
                )

        if not segments:
            raise RuntimeError("No speech segments were transcribed")

        # 5. combining segments into logical batches(by gaps and etc)
        batches = build_batches(segments, max_batch_chars=DEFAULT_MAX_BATCH_CHARS, max_batch_duration=DEFAULT_MAX_BATCH_DURATION, max_gap=DEFAULT_MAX_GAP)

        translator = GoogleTranslator(source="auto", target=args.lang)
        bg_audio, sr = load_mono_audio(str(bg_path))
        dubbed_audio = np.array(bg_audio, dtype=np.float32, copy=True)

        for index, batch in enumerate(batches, start=1):
            # 6. Translate the text
            translated_text = translate_text(translator, batch.text)
            logger.info("[%d/%d] %s", index, len(batches), translated_text)

            # 7. synthesize new voice
            voice = synthesize_xtts_batch(
                tts=tts,
                text=translated_text,
                lang=args.lang,
                gpt_cond_latent=gpt_cond_latent,
                speaker_embedding=speaker_embedding,
                temperature=DEFAULT_TTS_TEMPERATURE,
                max_tts_chars=DEFAULT_TTS_MAX_CHARS,
            )

            if voice.size == 0:
                continue

            # 8. resample in order to syncrhonize
            voice = resample_audio(voice, xtts_sr, sr)

            start_idx = max(0, int(round(batch.start * sr)))
            end_idx = min(len(dubbed_audio), int(round(batch.end * sr)))

            if end_idx <= start_idx:
                continue

            target_len = end_idx - start_idx
            # 9. aligning the length of a voice to the length of the time window
            voice = soft_fit_audio_to_length(
                audio=voice,
                sr=sr,
                target_len=target_len,
                min_tempo=DEFAULT_MIN_TEMPO,
                max_tempo=DEFAULT_MAX_TEMPO,
                stretch_tolerance=DEFAULT_STRETCH_TOLERANCE,
            )
            # 10. final mix voice + bg
            mix_voice_on_background(
                background=dubbed_audio,
                voice=voice,
                start_idx=start_idx,
                end_idx=end_idx,
                bg_gain=DEFAULT_BG_GAIN,
                voice_gain=DEFAULT_VOICE_GAIN,
            )

        peak = float(np.max(np.abs(dubbed_audio))) if dubbed_audio.size else 0.0
        if peak > 0.98:
            dubbed_audio = dubbed_audio * (0.98 / peak)

        sf.write(str(output_audio), dubbed_audio, sr)

    except Exception as exc:
        logger.exception("Error while dubbing: %s", exc)
        raise
    finally:
        if temp_dir.exists():
            shutil.rmtree(temp_dir, ignore_errors=True)


if __name__ == "__main__":
    main()
