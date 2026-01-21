# 🗺️ TravelMap - 유튜버 여행 지도
####
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
| **기타** | Docker |


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
- `startDate`: 시작 날짜 (ISO 8601)
- `endDate`: 종료 날짜 (ISO 8601)
- `page`: 페이지 번호 (기본값: 0)
- `size`: 페이지 크기 (기본값: 20)

