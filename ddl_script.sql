-- TravelMap 프로젝트 DDL 스크립트
-- PostgreSQL 데이터베이스용

-- 데이터베이스 생성 (필요시)
-- CREATE DATABASE travelmap;

-- 시퀀스 생성 (PostgreSQL IDENTITY 컬럼 지원)
CREATE SEQUENCE IF NOT EXISTS users_id_seq;
CREATE SEQUENCE IF NOT EXISTS videos_id_seq;
CREATE SEQUENCE IF NOT EXISTS visit_countries_id_seq;

-- 1. users 테이블 생성
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY DEFAULT nextval('users_id_seq'),
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    youtube_channel_id VARCHAR(50) UNIQUE,
    channel_url VARCHAR(255),
    profile_image_url VARCHAR(500),
    description TEXT,
    subscriber_count BIGINT,
    total_view_count BIGINT,
    total_video_count BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. videos 테이블 생성
CREATE TABLE IF NOT EXISTS videos (
    id BIGINT PRIMARY KEY DEFAULT nextval('videos_id_seq'),
    title VARCHAR(500) NOT NULL,
    video_id VARCHAR(20) NOT NULL UNIQUE,
    upload_date TIMESTAMP,
    thumbnail_url VARCHAR(500),
    description TEXT,
    view_count BIGINT,
    like_count BIGINT,
    comment_count BIGINT,
    duration VARCHAR(255),
    video_url VARCHAR(255),
    processed BOOLEAN DEFAULT FALSE,
    ocr_processed BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    user_id BIGINT NOT NULL,
    
    -- 외래 키 제약조건
    CONSTRAINT fk_videos_user_id 
        FOREIGN KEY (user_id) REFERENCES users(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- 3. visit_countries 테이블 생성
CREATE TABLE IF NOT EXISTS visit_countries (
    id BIGINT PRIMARY KEY DEFAULT nextval('visit_countries_id_seq'),
    country_code VARCHAR(5) NOT NULL,
    country_name VARCHAR(100) NOT NULL,
    country_emoji VARCHAR(10),
    continent VARCHAR(50),
    detection_method VARCHAR(20),
    confidence_score DOUBLE PRECISION,
    notes TEXT,
    visit_order INTEGER,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    video_id BIGINT NOT NULL,
    
    -- 외래 키 제약조건
    CONSTRAINT fk_visit_countries_video_id 
        FOREIGN KEY (video_id) REFERENCES videos(id) 
        ON DELETE CASCADE ON UPDATE CASCADE
);

-- 인덱스 생성 (성능 최적화)
-- users 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_users_youtube_channel_id ON users(youtube_channel_id);
CREATE INDEX IF NOT EXISTS idx_users_name ON users(name);

-- videos 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_videos_user_id ON videos(user_id);
CREATE INDEX IF NOT EXISTS idx_videos_video_id ON videos(video_id);
CREATE INDEX IF NOT EXISTS idx_videos_upload_date ON videos(upload_date);
CREATE INDEX IF NOT EXISTS idx_videos_processed ON videos(processed);
CREATE INDEX IF NOT EXISTS idx_videos_ocr_processed ON videos(ocr_processed);

-- visit_countries 테이블 인덱스
CREATE INDEX IF NOT EXISTS idx_visit_countries_video_id ON visit_countries(video_id);
CREATE INDEX IF NOT EXISTS idx_visit_countries_country_code ON visit_countries(country_code);
CREATE INDEX IF NOT EXISTS idx_visit_countries_continent ON visit_countries(continent);
CREATE INDEX IF NOT EXISTS idx_visit_countries_detection_method ON visit_countries(detection_method);

-- 제약조건 추가 (필요시)
-- detection_method에 대한 체크 제약조건
ALTER TABLE visit_countries 
ADD CONSTRAINT chk_detection_method 
CHECK (detection_method IN ('TITLE_EMOJI', 'OCR', 'MANUAL') OR detection_method IS NULL);

-- confidence_score 범위 제약조건
ALTER TABLE visit_countries 
ADD CONSTRAINT chk_confidence_score 
CHECK (confidence_score >= 0.0 AND confidence_score <= 1.0 OR confidence_score IS NULL);

-- 트리거 생성 (updated_at 자동 업데이트)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- users 테이블에 updated_at 트리거 적용
CREATE TRIGGER update_users_updated_at 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- videos 테이블에 updated_at 트리거 적용
CREATE TRIGGER update_videos_updated_at 
    BEFORE UPDATE ON videos 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- visit_countries 테이블에 updated_at 트리거 적용
CREATE TRIGGER update_visit_countries_updated_at 
    BEFORE UPDATE ON visit_countries 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- 시퀀스 소유자 설정
ALTER SEQUENCE users_id_seq OWNED BY users.id;
ALTER SEQUENCE videos_id_seq OWNED BY videos.id;
ALTER SEQUENCE visit_countries_id_seq OWNED BY visit_countries.id;

-- 샘플 데이터 삽입 (테스트용 - 선택사항)
/*
INSERT INTO users (name, gender, youtube_channel_id, channel_url) VALUES
('김여행', '여성', 'UC1234567890', 'https://youtube.com/@kimtravel'),
('박모험', '남성', 'UC0987654321', 'https://youtube.com/@parkadventure');

INSERT INTO videos (title, video_id, user_id, upload_date) VALUES
('일본 도쿄 여행 VLOG', 'v1234567890', 1, '2024-01-15 10:00:00'),
('태국 방콕 맛집 투어', 'v0987654321', 1, '2024-01-20 14:30:00');

INSERT INTO visit_countries (country_code, country_name, country_emoji, continent, detection_method, video_id) VALUES
('JP', '일본', '🇯🇵', '아시아', 'TITLE_EMOJI', 1),
('TH', '태국', '🇹🇭', '아시아', 'TITLE_EMOJI', 2);
*/ 