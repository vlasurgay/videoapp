import sys
import os
from faster_whisper import WhisperModel

os.environ["KMP_DUPLICATE_LIB_OK"] = "TRUE"

model = WhisperModel("base", device="cpu", compute_type="int8")

def main():
    segments, info = model.transcribe(sys.stdin.buffer, beam_size=1)

    print(info.language)

if __name__ == "__main__":
    main()