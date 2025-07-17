# 🗺️ TravelMap - 유튜버 여행 지도

유튜버들의 여행 영상을 지도에서 시각화하여 볼 수 있는 웹 애플리케이션입니다.

## ✨ 주요 기능

- 🌍 **세계 지도 시각화**: 유튜버들이 방문한 국가를 지도에서 확인
- 🎬 **여행 영상 수집**: YouTube API를 통한 자동 영상 메타데이터 수집
- 🏳️ **국가 자동 탐지**: 영상 제목의 국기 이모지와 키워드로 방문 국가 추출
- 🔍 **다양한 필터**: 유튜버, 국가, 성별, 연도별 필터링
- 📊 **통계 대시보드**: 방문 국가 수, 총 방문 횟수, 영상 수 등 통계 제공
- 🎯 **인터랙티브 UI**: 지도 클릭으로 해당 국가 영상 바로 확인

## 🏗️ 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   React + Redux │    │ Spring Boot 3.x │    │   PostgreSQL    │
│   (Frontend)    │◄──►│   (Backend)     │◄──►│   (Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                     ┌─────────────────┐
                     │  YouTube API    │
                     │  (Data Source)  │
                     └─────────────────┘
```

### 🔧 기술 스택

| 계층 | 기술 |
|------|------|
| **프론트엔드** | React 18, TypeScript, Redux Toolkit, Material-UI, react-simple-maps |
| **백엔드** | Java 19, Spring Boot 3.x, Spring Data JPA, Spring Scheduler |
| **데이터베이스** | PostgreSQL 15 |
| **외부 API** | YouTube Data API v3 |
| **기타** | Docker, Tesseract OCR, Nginx |

## 🚀 빠른 시작

### Prerequisites

- Docker & Docker Compose
- YouTube Data API v3 키 ([발급 방법](https://console.developers.google.com/))

### 1. 프로젝트 클론

```bash
git clone https://github.com/your-username/TravelMap.git
cd TravelMap
```

### 2. 환경변수 설정

```bash
# .env 파일 생성
cat > .env << EOF
YOUTUBE_API_KEY=your-youtube-api-key-here
POSTGRES_DB=travelmap
POSTGRES_USER=travelmap
POSTGRES_PASSWORD=travelmap123
SPRING_PROFILES_ACTIVE=docker
REACT_APP_API_URL=http://localhost:8080/api
EOF
```

### 3. 애플리케이션 실행

```bash
# 모든 서비스 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f
```

### 4. 접속

- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080/api
- **데이터베이스**: localhost:5432

## 🔧 개발 환경 설정

### 백엔드 개발

```bash
cd backend

# Gradle 빌드
./gradlew build

# 로컬 실행 (PostgreSQL 필요)
./gradlew bootRun
```

### 프론트엔드 개발

```bash
cd frontend

# 의존성 설치
npm install

# 개발 서버 실행
npm start
```

## 📝 API 문서

### 주요 엔드포인트

| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/api/filters` | 필터 옵션 목록 조회 |
| `GET` | `/api/map-data` | 지도 시각화 데이터 조회 |
| `GET` | `/api/videos` | 영상 목록 조회 |
| `GET` | `/api/videos/{id}` | 개별 영상 상세 조회 |
| `GET` | `/api/countries/{countryCode}/videos` | 특정 국가 영상 목록 |

### 필터 파라미터

- `userId`: 유튜버 ID
- `countryCode`: 국가 코드 (ISO 2자리)
- `gender`: 성별
- `startDate`: 시작 날짜 (ISO 8601)
- `endDate`: 종료 날짜 (ISO 8601)
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 20)

## 🗃️ 데이터베이스 스키마

### 주요 테이블

```sql
-- 유튜버 정보
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    youtube_channel_id VARCHAR(50) UNIQUE,
    channel_url VARCHAR(255),
    profile_image_url VARCHAR(500),
    subscriber_count BIGINT,
    total_video_count BIGINT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 영상 정보
CREATE TABLE videos (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    video_id VARCHAR(20) UNIQUE NOT NULL,
    upload_date TIMESTAMP,
    thumbnail_url VARCHAR(500),
    view_count BIGINT,
    like_count BIGINT,
    duration VARCHAR(20),
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- 방문 국가 정보
CREATE TABLE visit_countries (
    id BIGSERIAL PRIMARY KEY,
    country_code VARCHAR(5) NOT NULL,
    country_name VARCHAR(100) NOT NULL,
    country_emoji VARCHAR(10),
    continent VARCHAR(50),
    detection_method VARCHAR(20),
    confidence_score DOUBLE PRECISION,
    video_id BIGINT REFERENCES videos(id),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

## 🎯 사용 방법

### 1. 유튜버 채널 추가

백엔드 API를 통해 유튜버 채널을 추가할 수 있습니다:

```bash
curl -X POST http://localhost:8080/api/channels \
  -H "Content-Type: application/json" \
  -d '{"channelId": "UC_CHANNEL_ID_HERE"}'
```

### 2. 영상 데이터 수집

Spring Scheduler가 주기적으로 실행되어 새로운 영상을 수집합니다. 수동으로 실행하려면:

```bash
curl -X POST http://localhost:8080/api/collect/videos
```

### 3. 국가 데이터 처리

영상 제목에서 국가 정보를 추출합니다:

```bash
curl -X POST http://localhost:8080/api/process/countries
```

## 🔍 주요 기능 상세

### 국가 탐지 알고리즘

1. **국기 이모지 탐지**: 영상 제목에서 🇰🇷, 🇯🇵 등의 국기 이모지 추출
2. **키워드 매칭**: '한국', 'Korea', '도쿄', 'Tokyo' 등의 국가/도시명 매칭
3. **신뢰도 점수**: 탐지 방법에 따른 신뢰도 점수 부여
4. **중복 제거**: 동일 영상의 중복 국가 제거

### 지도 시각화

- **색상 구분**: 방문 횟수에 따른 색상 강도 변경
- **툴팁**: 마우스 오버시 국가 정보 및 유튜버 목록 표시
- **클릭 이벤트**: 국가 클릭시 해당 국가 영상 필터링

## 🐳 Docker 명령어

```bash
# 전체 서비스 실행
docker-compose up -d

# 특정 서비스만 재실행
docker-compose restart backend

# 로그 확인
docker-compose logs -f backend

# 데이터베이스 접속
docker-compose exec postgres psql -U travelmap -d travelmap

# 전체 중지 및 데이터 삭제
docker-compose down -v
```

## 🔧 트러블슈팅

### 자주 발생하는 문제

1. **YouTube API Quota 초과**
   - 일일 10,000 units 제한
   - 해결: API 키 교체 또는 다음날 시도

2. **OCR 인식 실패**
   - Tesseract 언어팩 부족
   - 해결: 필요한 언어팩 설치

3. **지도 로딩 실패**
   - 네트워크 연결 문제
   - 해결: CDN 주소 확인

4. **Docker 메모리 부족**
   - 해결: Docker Desktop 메모리 할당 증가

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 `LICENSE` 파일을 참조하세요.

## 📞 연락처

프로젝트 링크: [https://github.com/your-username/TravelMap](https://github.com/your-username/TravelMap)

---

**⭐ 이 프로젝트가 도움이 되었다면 별표를 눌러주세요!**