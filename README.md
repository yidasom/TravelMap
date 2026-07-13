# 🗺️ TravelMap - 유튜버 여행 지도

유튜버들의 여행 영상을 세계 지도에서 시각화해서 볼 수 있는 웹 애플리케이션. 채널을 등록하면 영상 제목에서 국기 이모지·국가명·도시명을 자동으로 인식해 지도에 방문 국가별 색상과 마커로 표시한다.

## ✨ 주요 기능

- 🌍 **세계 지도 시각화**: 방문 국가를 방문 횟수에 따라 색상으로 표시(choropleth), 방문 지점은 원형 클러스터 마커로 표시
- 🎬 **여행 영상 자동 수집**: YouTube Data API v3로 채널 정보 및 최신 영상 메타데이터 수집
- 🏳️ **국가·도시 자동 탐지**: 영상 제목의 국기 이모지, 국가명/도시명 키워드로 방문 국가와 도시를 추출
- 🛠️ **키워드 관리 화면**: 감지 안 되는 국가/도시가 있으면 코드 수정 없이 관리자 화면에서 키워드를 직접 추가/삭제
- 🔍 **다양한 필터**: 유튜버, 국가, 연도, 대륙, 기간별 필터링
- 📊 **통계 대시보드**: 방문 국가 수, 총 방문 횟수, 여행 경로 수 등 통계 제공
- 🎯 **인터랙티브 지도**: 국가 클릭/호버로 방문 정보 확인, 클릭 시 해당 국가로 필터링
- 📹 **영상 목록**: 썸네일형(그리드)/목록형 전환, 페이지네이션("더 보기")
- 🗂️ **데이터 수집 관리 화면**: 전체 수집/업데이트, 미처리 영상 처리, 개별 채널 추가를 관리자 UI에서 실행

## 🏗️ 시스템 아키텍처

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  React + Redux  │    │  Spring Boot 3  │    │   PostgreSQL    │
│   (Frontend)    │◄──►│   (Backend)     │◄──►│   (Database)    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                     ┌─────────────────┐
                     │  YouTube Data   │
                     │  API v3         │
                     └─────────────────┘
```

### 🔧 기술 스택

| 계층 | 기술 |
|------|------|
| **프론트엔드** | React 18, TypeScript, Redux Toolkit, Material-UI, react-leaflet(Leaflet.js), axios |
| **백엔드** | Java 21, Spring Boot 3.4, Spring Data JPA, Spring Quartz |
| **데이터베이스** | PostgreSQL 16 |
| **외부 API** | YouTube Data API v3 |
| **지도 데이터** | Natural Earth 50m (국가 경계 GeoJSON) |
| **인프라** | Docker / docker-compose (K8s, Helm, Jenkins 설정도 `k8s/`, `helm/`, `Jenkinsfile.*`에 포함) |

## 🚀 시작하기 (로컬 실행)

### 1. 사전 준비

- Docker, docker-compose
- YouTube Data API v3 키 ([Google Cloud Console](https://console.cloud.google.com)에서 프로젝트 생성 → "YouTube Data API v3" 사용 설정 → API 키 발급)

### 2. 전체 스택 실행

```bash
docker-compose up --build
```

- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api
- PostgreSQL: localhost:5432 (DB `travelmap` / user `travelmap` / password `travelmap`)

```bash
docker-compose up -d --build backend    # 백엔드만
docker-compose up -d --build frontend   # 프론트엔드만
```

### 3. 첫 채널 등록

프론트엔드 상단의 **데이터 수집 관리** 패널을 펼쳐서 "채널 추가"로 유튜브 채널명을 검색하거나, API로 직접 호출할 수 있다:

```bash
curl -X POST "http://localhost:8080/api/admin/add-channel" --data-urlencode "searchQuery=채널명" -G
```

## 📁 프로젝트 구조

```
backend/src/main/java/com/travelmap/
├── controller/   # TravelMapController(조회), AdminController(수집), KeywordController(키워드 관리)
├── service/      # DataCollectionService, YouTubeService, CountryDetectionService
├── entity/       # User, Video, VisitCountry, CountryKeyword, CityKeyword
├── repository/   # Spring Data JPA 리포지토리
├── dto/          # API 응답/요청 DTO
└── config/       # YouTubeConfig, KeywordSeeder(최초 기동 시 키워드 시드 데이터 삽입)

frontend/src/
├── components/
│   ├── map/                    # WorldMap 하위 컴포넌트 (범례, 통계, 클러스터 마커, 바운드 처리)
│   ├── WorldMap.tsx             # 지도 메인 컴포넌트
│   ├── VideoList.tsx            # 영상 목록 (그리드/리스트 뷰)
│   ├── FilterPanel.tsx          # 필터 UI
│   ├── DataCollectionPanel.tsx  # 데이터 수집 관리 UI
│   └── KeywordManagementPanel.tsx # 국가/도시 키워드 관리 UI
├── store/        # Redux Toolkit (appSlice)
├── services/     # api.ts (axios 클라이언트)
└── types/        # 공용 타입 정의
```

## 📝 API 문서

### 조회 API (`TravelMapController`)

| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/api/filters` | 필터 옵션(유튜버/국가/연도/대륙) 목록 조회 |
| `GET` | `/api/map-data` | 지도 시각화 데이터 조회 (국가별 방문 집계) |
| `GET` | `/api/videos` | 영상 목록 조회 (페이지네이션) |
| `GET` | `/api/videos/{id}` | 개별 영상 상세 조회 |
| `GET` | `/api/countries/{countryCode}/videos` | 특정 국가의 영상 목록 |

`/map-data`, `/videos` 공통 쿼리 파라미터:

- `userId`: 유튜버 ID
- `countryCode`: 국가 코드 (ISO 2자리)
- `continent`: 대륙
- `year`: 연도
- `startDate` / `endDate`: 기간 (ISO 8601)
- `page` / `size`: 페이지네이션 (`/videos`만, 기본값 0 / 20)

### 데이터 수집 관리 API (`AdminController`)

| Method | URL | 설명 |
|--------|-----|------|
| `POST` | `/api/admin/collect-all` | 등록된 모든 채널의 최신 영상 수집 + 국가 감지 |
| `POST` | `/api/admin/collect-channel?searchQuery=` | 특정 채널 영상 수집 + 국가 감지 |
| `POST` | `/api/admin/update-all` | 모든 채널 정보/최신 영상 갱신 (국가 감지는 별도) |
| `POST` | `/api/admin/process-unprocessed` | 미처리 영상들의 상세 정보 업데이트 + 국가 감지 |
| `GET` | `/api/admin/collection-status` | 현재 수집 작업 진행 상태 조회 |
| `POST` | `/api/admin/add-channel?searchQuery=&channelName=` | 새 채널 등록 + 영상 수집 + 국가 감지 |

### 국가/도시 키워드 관리 API (`KeywordController`)

| Method | URL | 설명 |
|--------|-----|------|
| `GET` | `/api/admin/keywords/countries` | 국가 키워드 목록 조회 |
| `POST` | `/api/admin/keywords/countries` | 국가 키워드 추가 (`keyword`, `countryCode`, `countryName`, `continent`, `countryEmoji`) |
| `DELETE` | `/api/admin/keywords/countries/{id}` | 국가 키워드 삭제 |
| `GET` | `/api/admin/keywords/cities` | 도시 키워드 목록 조회 |
| `POST` | `/api/admin/keywords/cities` | 도시 키워드 추가 (`keyword`, `cityName`, `latitude`, `longitude`, `countryCode`, `countryName`, `continent`, `countryEmoji`) |
| `DELETE` | `/api/admin/keywords/cities/{id}` | 도시 키워드 삭제 |

## 🏳️ 국가/도시는 어떻게 감지되나

영상을 수집(또는 "미처리 영상 처리")할 때 딱 한 번, **영상 제목 텍스트만** 분석해서 결과를 DB(`visit_countries` 테이블)에 저장해둔다. 지도를 열 때마다 다시 분석하지 않고 저장된 결과를 읽기만 한다. 썸네일 이미지나 유튜브 태그는 읽지 않는다.

감지 우선순위:
1. 제목에 포함된 국기 이모지 (예: 🇹🇭)
2. 제목에 포함된 도시명 키워드 (예: "치앙마이" → 태국 + 도시명 + 좌표까지 저장)
3. 제목에 포함된 국가명 키워드 (예: "태국")
4. 키워드로 못 찾으면 **외부 지오코딩 폴백**: 제목을 단어 단위로 쪼개 OpenStreetMap Nominatim API로 조회해서 국가/도시급 지명인지 확인. 확인되면 그 지명을 **키워드 테이블에 자동 등록**해서 다음부터는 API 호출 없이 1~3단계에서 바로 잡힌다 (`detection_method = GEOCODED`)
5. 그래도 못 찾으면 기본값(대한민국)으로 저장

국가/도시 키워드는 `country_keywords` / `city_keywords` 테이블에 저장되며, 최초 기동 시 `KeywordSeeder`가 기본 키워드 세트를 심어준다. 지오코딩 자가 학습 덕분에 대부분의 새 지명은 자동으로 등록되고, 프론트엔드의 "국가·도시 키워드 관리" 패널(또는 위 키워드 관리 API)은 **오탐 삭제나 예외 등록용**으로 쓰면 된다. 키워드 추가/학습은 **이미 수집된 영상에 소급 적용되지 않으므로**, 반영하려면 해당 영상들을 미처리 상태로 되돌린 뒤 `/api/admin/process-unprocessed`를 실행해야 한다. 지오코딩 폴백은 `geocoding.enabled=false` 프로퍼티로 끌 수 있다.

## ⚠️ 알려진 제약

- 국가/도시 감지는 제목 텍스트 기반이라, 제목에 지명이 아예 없는 영상(쇼츠 등)은 감지할 수 없다. 썸네일 이미지·영상 내용은 읽지 않는다.
- 지오코딩 폴백은 Nominatim 정책상 초당 1회 호출로 제한되어, 새 지명이 많은 대량 재처리는 몇 분 정도 걸릴 수 있다. 드물게 일반 단어가 지명으로 오인될 수 있는데, 이 경우 키워드 관리 패널에서 잘못 학습된 키워드를 삭제하면 된다.
- 지도 국가 경계 데이터(Natural Earth 50m)에 포함되지 않는 초소형 국가는 색칠이 안 될 수 있다.
- YouTube Data API 무료 할당량(일 10,000 유닛) 내에서 동작하므로, "전체 수집/업데이트"를 과도하게 반복하면 할당량이 소진될 수 있다.
