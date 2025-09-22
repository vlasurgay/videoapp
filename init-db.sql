CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    creation_time BIGINT
);

CREATE TABLE IF NOT EXISTS video_metadata (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    format VARCHAR(255),
    duration DOUBLE PRECISION,
    height INT,
    width INT,
    bitrate BIGINT,
    has_audio BOOLEAN,
    file_size_in_bytes BIGINT,
    s3_key VARCHAR(500),
    video_id BIGINT
);

CREATE TABLE IF NOT EXISTS video_files (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(50) NOT NULL,
    user_id BIGINT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    creation_time BIGINT NOT NULL,
    update_time BIGINT,
    original_metadata_id BIGINT UNIQUE
);

CREATE TABLE IF NOT EXISTS upload_info (
    id BIGSERIAL PRIMARY KEY,
    public_id VARCHAR(255) NOT NULL,
    upload_id VARCHAR(255) NOT NULL,
    user_id BIGINT,
    video_id BIGINT UNIQUE NOT NULL,
    file_name VARCHAR(255),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    creation_time BIGINT NOT NULL,
    expires_at BIGINT NOT NULL,
    total_parts INT NOT NULL,
    status VARCHAR(50),
    original_metadata_id BIGINT UNIQUE,
    target_formats TEXT,
    target_resolutions TEXT,
    target_codecs TEXT,
    muted BOOLEAN
);

ALTER TABLE video_metadata
    ADD CONSTRAINT fk_metadata_video FOREIGN KEY (video_id) REFERENCES video_files(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE video_files
    ADD CONSTRAINT fk_video_user FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_video_metadata FOREIGN KEY (original_metadata_id) REFERENCES video_metadata(id) DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE upload_info
    ADD CONSTRAINT fk_upload_user FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_upload_video FOREIGN KEY (video_id) REFERENCES video_files(id),
    ADD CONSTRAINT fk_upload_metadata FOREIGN KEY (original_metadata_id) REFERENCES video_metadata(id) DEFERRABLE INITIALLY DEFERRED;

CREATE UNIQUE INDEX IF NOT EXISTS idx_video_public_id ON video_files(public_id);