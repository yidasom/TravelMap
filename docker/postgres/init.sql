-- TravelMap Database Initialization Script

-- Create database if not exists (already created by Docker)
-- CREATE DATABASE IF NOT EXISTS travelmap;

-- Use the database
\c travelmap;

-- Create extensions if needed
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Set timezone
SET timezone = 'Asia/Seoul';

-- Create sample data for testing (optional)
-- Users table will be auto-created by Hibernate

-- Insert sample users for testing
-- Note: These will be inserted after the application starts and creates tables
-- You can use these INSERT statements manually for testing

-- Sample users
/*
INSERT INTO users (name, gender, youtube_channel_id, channel_url, profile_image_url, created_at, updated_at) 
VALUES 
    ('여행 유튜버 A', '여성', 'UC_sample_1', 'https://youtube.com/channel/UC_sample_1', 'https://example.com/profile1.jpg', NOW(), NOW()),
    ('여행 유튜버 B', '남성', 'UC_sample_2', 'https://youtube.com/channel/UC_sample_2', 'https://example.com/profile2.jpg', NOW(), NOW()),
    ('여행 유튜버 C', '여성', 'UC_sample_3', 'https://youtube.com/channel/UC_sample_3', 'https://example.com/profile3.jpg', NOW(), NOW());
*/

-- Grant permissions
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO travelmap;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO travelmap;

-- Print success message
\echo 'TravelMap database initialized successfully!' 