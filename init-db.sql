CREATE TYPE video_status AS ENUM ('UPLOADING', 'PROCESSING', 'COMPLETED', 'FAILED', 'ABORTED');
CREATE TYPE job_status AS ENUM ('PENDING', 'PROCESSING', 'RETRY_WAIT', 'COMPLETED', 'FAILED', 'ABORTED');

CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS videos (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(50) NOT NULL UNIQUE,
    user_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status video_status DEFAULT 'UPLOADING',
    master_playlist_key VARCHAR(500),
    source_video_key VARCHAR(500),
    source_audio_key VARCHAR(500),
    target_settings JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS upload_infos (
    id BIGSERIAL PRIMARY KEY,
    video_id BIGINT NOT NULL UNIQUE REFERENCES videos(id) ON DELETE CASCADE,
    upload_id VARCHAR(255) NOT NULL,
    upload_origin_key VARCHAR(500) NOT NULL,
    base_metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE IF NOT EXISTS media_tracks (
    id BIGSERIAL PRIMARY KEY,
    video_id BIGINT NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    label VARCHAR(50) NOT NULL,
    upload_key VARCHAR(500) NOT NULL,
    metadata JSONB DEFAULT '{}',
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS processing_jobs (
    id BIGSERIAL PRIMARY KEY,
    video_id BIGINT NOT NULL REFERENCES videos(id) ON DELETE CASCADE,
    type VARCHAR(50) NOT NULL,
    status job_status DEFAULT 'PENDING',
    payload JSONB DEFAULT '{}',
    attempt INT DEFAULT 0,
    max_attempts INT DEFAULT 3,
    last_error TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    next_retry_at TIMESTAMPTZ
);

CREATE TABLE job_dependencies (
    dependent_job_id BIGINT NOT NULL REFERENCES processing_jobs(id) ON DELETE CASCADE,
    depends_on_job_id BIGINT NOT NULL REFERENCES processing_jobs(id) ON DELETE CASCADE,
    PRIMARY KEY (dependent_job_id, depends_on_job_id)
);

CREATE INDEX idx_videos_public_id ON videos(public_id);
CREATE INDEX idx_videos_status ON videos(status);
CREATE INDEX idx_upload_infos_video_id ON upload_infos(video_id);
CREATE INDEX idx_processing_jobs_video_id ON processing_jobs(video_id);
CREATE INDEX idx_processing_jobs_picker ON processing_jobs (created_at)WHERE status IN ('PENDING', 'RETRY_WAIT');