package com.travelmap.service;

import com.travelmap.entity.CityKeyword;
import com.travelmap.entity.CountryKeyword;
import com.travelmap.entity.Video;
import com.travelmap.entity.VisitCountry;
import com.travelmap.repository.CityKeywordRepository;
import com.travelmap.repository.CountryKeywordRepository;
import com.travelmap.repository.VisitCountryRepository;
import com.travelmap.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.Objects;

@Service
@Transactional
public class CountryDetectionService {

    private static final Logger logger = LoggerFactory.getLogger(CountryDetectionService.class);

    private final VisitCountryRepository visitCountryRepository;
    private final VideoRepository videoRepository;
    private final CountryKeywordRepository countryKeywordRepository;
    private final CityKeywordRepository cityKeywordRepository;
    private final GeocodingService geocodingService;

    // 대한민국 기본 정보
    private static final CountryInfo DEFAULT_COUNTRY = new CountryInfo("KR", "대한민국", "Asia", "🇰🇷");

    // 국기 이모지와 국가 코드/이름 매핑 (이 매핑은 거의 안 바뀌어서 DB로 옮기지 않고 그대로 둔다)
    private static final Map<String, CountryInfo> FLAG_EMOJI_MAP = new HashMap<>();

    static {
        initializeFlagEmojiMap();
    }

    @Autowired
    public CountryDetectionService(VisitCountryRepository visitCountryRepository,
                                    VideoRepository videoRepository,
                                    CountryKeywordRepository countryKeywordRepository,
                                    CityKeywordRepository cityKeywordRepository,
                                    GeocodingService geocodingService) {
        this.visitCountryRepository = visitCountryRepository;
        this.videoRepository = videoRepository;
        this.countryKeywordRepository = countryKeywordRepository;
        this.cityKeywordRepository = cityKeywordRepository;
        this.geocodingService = geocodingService;
    }

    /**
     * 영상 제목에서 국가(및 가능하면 도시) 정보를 추출하고 저장
     */
    public List<VisitCountry> extractCountriesFromTitle(Video video) {
        logger.info("영상 제목에서 국가 추출 시작: {}", video.getTitle());

        // 기본 반환값
        List<VisitCountry> savedCountries = new ArrayList<>();

        try {
            // 입력 검증
            if (video == null) {
                logger.warn("Video 객체가 null입니다. 국가 추출을 건너뜁니다.");
                return savedCountries;
            }

            if (video.getId() == null) {
                logger.warn("Video ID가 null입니다. 국가 추출을 건너뜁니다: {}", video.getVideoId());
                return savedCountries;
            }

            if (video.getTitle() == null || video.getTitle().trim().isEmpty()) {
                logger.warn("Video 제목이 비어있습니다. 기본값 사용: {}", video.getVideoId());
                return saveDefaultCountry(video);
            }

            // Video 객체를 영속성 컨텍스트에서 다시 조회하여 detached 상태 방지
            Video persistentVideo;
            try {
                persistentVideo = videoRepository.findById(video.getId())
                        .orElseThrow(() -> new RuntimeException("Video not found: " + video.getId()));
            } catch (Exception e) {
                logger.warn("Video 조회 실패, 원본 객체 사용: {} - {}", video.getId(), e.getMessage());
                persistentVideo = video;
            }

            List<DetectedLocation> detectedLocations = new ArrayList<>();

            // 1. 국기 이모지로 국가 탐지 (예외 처리)
            try {
                detectedLocations.addAll(detectCountriesByFlagEmoji(persistentVideo.getTitle()));
            } catch (Exception e) {
                logger.warn("국기 이모지 탐지 실패: {} - {}", persistentVideo.getVideoId(), e.getMessage());
            }

            // 2. 도시명 키워드로 국가+도시 탐지 (예외 처리)
            try {
                detectedLocations.addAll(detectCitiesByKeywords(persistentVideo.getTitle()));
            } catch (Exception e) {
                logger.warn("도시 키워드 탐지 실패: {} - {}", persistentVideo.getVideoId(), e.getMessage());
            }

            // 3. 국가명 키워드로 국가 탐지 (예외 처리)
            try {
                detectedLocations.addAll(detectCountriesByKeywords(persistentVideo.getTitle()));
            } catch (Exception e) {
                logger.warn("키워드 탐지 실패: {} - {}", persistentVideo.getVideoId(), e.getMessage());
            }

            // 중복 제거 (예외 처리) - 같은 국가가 여러 번 감지되면 도시 정보가 있는 쪽을 우선한다
            try {
                detectedLocations = removeDuplicateDetectedLocations(detectedLocations);
            } catch (Exception e) {
                logger.warn("중복 제거 실패: {} - {}", persistentVideo.getVideoId(), e.getMessage());
                // 중복 제거 실패해도 계속 진행
            }

            // 4. 키워드 사전으로 못 찾은 경우 외부 지오코딩(Nominatim)으로 지명 탐지 시도
            //    (성공하면 키워드 테이블에 자동 등록되어 다음부터는 1~3단계에서 바로 잡힌다)
            if (detectedLocations.isEmpty()) {
                try {
                    detectedLocations.addAll(geocodingService.detectLocationsFromTitle(persistentVideo.getTitle()));
                } catch (Exception e) {
                    logger.warn("지오코딩 탐지 실패: {} - {}", persistentVideo.getVideoId(), e.getMessage());
                }
            }

            // 5. 그래도 못 찾은 경우 기본값(대한민국) 사용
            if (detectedLocations.isEmpty()) {
                logger.info("제목에서 국가를 찾지 못해 기본값 사용: {}", persistentVideo.getVideoId());
                detectedLocations.add(new DetectedLocation(DEFAULT_COUNTRY));
            }

            // 6. 데이터베이스에 저장 또는 업데이트 (예외 처리)
            try {
                savedCountries = saveOrUpdateCountries(persistentVideo, detectedLocations);
            } catch (Exception e) {
                logger.warn("국가 정보 저장 실패, 기본값으로 재시도: {} - {}", persistentVideo.getVideoId(), e.getMessage());
                // 저장 실패 시 기본값으로라도 저장 시도
                try {
                    savedCountries = saveDefaultCountry(persistentVideo);
                } catch (Exception fallbackError) {
                    logger.error("기본값 저장도 실패: {} - {}", persistentVideo.getVideoId(), fallbackError.getMessage());
                    // 완전히 실패해도 빈 리스트 반환하고 계속 진행
                }
            }

            logger.info("영상에서 {}개 국가 처리 완료: {}", savedCountries.size(), video.getVideoId());

        } catch (Exception e) {
            logger.error("국가 정보 추출 중 예상치 못한 오류 발생, 건너뜀: {} - {}",
                    video != null ? video.getVideoId() : "unknown", e.getMessage());
            // 어떤 오류가 발생해도 빈 리스트 반환하고 다음 비디오로 진행
        }

        return savedCountries;
    }

    /**
     * 국기 이모지로 국가 탐지 (이모지만으로는 도시까지 알 수 없음)
     */
    private List<DetectedLocation> detectCountriesByFlagEmoji(String title) {
        List<DetectedLocation> locations = new ArrayList<>();

        try {
            if (title == null || title.trim().isEmpty()) {
                return locations;
            }

            for (Map.Entry<String, CountryInfo> entry : FLAG_EMOJI_MAP.entrySet()) {
                try {
                    if (title.contains(entry.getKey())) {
                        locations.add(new DetectedLocation(entry.getValue()));
                        logger.debug("국기 이모지로 국가 탐지: {} ({})", entry.getValue().getName(), entry.getKey());
                    }
                } catch (Exception e) {
                    logger.warn("개별 국기 이모지 탐지 실패: {} - {}", entry.getKey(), e.getMessage());
                    // 개별 이모지 탐지 실패해도 계속 진행
                    continue;
                }
            }
        } catch (Exception e) {
            logger.warn("국기 이모지 탐지 전체 실패: {}", e.getMessage());
        }

        return locations;
    }

    /**
     * 도시명 키워드로 국가+도시 탐지 (키워드는 DB의 city_keywords 테이블에서 불러온다 - 관리자 API로 추가/삭제 가능)
     */
    private List<DetectedLocation> detectCitiesByKeywords(String title) {
        List<DetectedLocation> locations = new ArrayList<>();

        try {
            if (title == null || title.trim().isEmpty()) {
                return locations;
            }

            String titleLower = title.toLowerCase();

            for (CityKeyword cityKeyword : cityKeywordRepository.findAll()) {
                try {
                    String keyword = cityKeyword.getKeyword().toLowerCase();

                    if (titleLower.contains(keyword)) {
                        CountryInfo country = new CountryInfo(cityKeyword.getCountryCode(), cityKeyword.getCountryName(),
                                cityKeyword.getContinent(), cityKeyword.getCountryEmoji());
                        locations.add(new DetectedLocation(country, cityKeyword.getCityName(),
                                cityKeyword.getLatitude(), cityKeyword.getLongitude()));
                        logger.debug("키워드로 도시 탐지: {} ({}, 키워드: {})", cityKeyword.getCityName(), cityKeyword.getCountryName(), keyword);
                    }
                } catch (Exception e) {
                    logger.warn("개별 도시 키워드 탐지 실패: {} - {}", cityKeyword.getKeyword(), e.getMessage());
                    // 개별 키워드 탐지 실패해도 계속 진행
                    continue;
                }
            }
        } catch (Exception e) {
            logger.warn("도시 키워드 탐지 전체 실패: {}", e.getMessage());
        }

        return locations;
    }

    /**
     * 국가명 키워드로 국가 탐지 (키워드는 DB의 country_keywords 테이블에서 불러온다 - 관리자 API로 추가/삭제 가능)
     */
    private List<DetectedLocation> detectCountriesByKeywords(String title) {
        List<DetectedLocation> locations = new ArrayList<>();

        try {
            if (title == null || title.trim().isEmpty()) {
                return locations;
            }

            String titleLower = title.toLowerCase();

            for (CountryKeyword countryKeyword : countryKeywordRepository.findAll()) {
                try {
                    String keyword = countryKeyword.getKeyword().toLowerCase();

                    if (titleLower.contains(keyword)) {
                        CountryInfo country = new CountryInfo(countryKeyword.getCountryCode(), countryKeyword.getCountryName(),
                                countryKeyword.getContinent(), countryKeyword.getCountryEmoji());
                        locations.add(new DetectedLocation(country));
                        logger.debug("키워드로 국가 탐지: {} (키워드: {})", countryKeyword.getCountryName(), keyword);
                    }
                } catch (Exception e) {
                    logger.warn("개별 키워드 탐지 실패: {} - {}", countryKeyword.getKeyword(), e.getMessage());
                    // 개별 키워드 탐지 실패해도 계속 진행
                    continue;
                }
            }
        } catch (Exception e) {
            logger.warn("키워드 탐지 전체 실패: {}", e.getMessage());
        }

        return locations;
    }

    /**
     * 중복 국가 제거 (감지된 DetectedLocation 리스트에서).
     * 같은 국가가 여러 방식으로 감지되면, 도시 정보가 있는 쪽을 우선한다.
     */
    private List<DetectedLocation> removeDuplicateDetectedLocations(List<DetectedLocation> locations) {
        try {
            if (locations == null || locations.isEmpty()) {
                return new ArrayList<>();
            }

            Map<String, DetectedLocation> uniqueLocations = new LinkedHashMap<>();

            for (DetectedLocation location : locations) {
                try {
                    if (location == null || location.getCountry() == null || location.getCountry().getCode() == null) {
                        continue;
                    }

                    String code = location.getCountry().getCode();
                    DetectedLocation existing = uniqueLocations.get(code);

                    if (existing == null || (existing.getCityName() == null && location.getCityName() != null)) {
                        uniqueLocations.put(code, location);
                    }
                } catch (Exception e) {
                    logger.warn("개별 국가 중복 제거 실패: {} - {}",
                            location != null && location.getCountry() != null ? location.getCountry().getCode() : "null", e.getMessage());
                    // 개별 국가 처리 실패해도 계속 진행
                    continue;
                }
            }

            return new ArrayList<>(uniqueLocations.values());
        } catch (Exception e) {
            logger.warn("중복 제거 전체 실패: {}", e.getMessage());
            // 중복 제거 실패 시 원본 리스트 반환
            return locations != null ? locations : new ArrayList<>();
        }
    }

    /**
     * 국가(및 도시) 정보를 데이터베이스에 저장하거나 업데이트
     */
    private List<VisitCountry> saveOrUpdateCountries(Video video, List<DetectedLocation> detectedLocations) {
        List<VisitCountry> savedCountries = new ArrayList<>();

        try {
            // Video 객체 검증
            if (video == null || video.getId() == null) {
                logger.error("Video 객체가 null이거나 ID가 없습니다.");
                return savedCountries;
            }

            // 기존 해당 비디오의 국가 정보 조회
            List<VisitCountry> existingCountries;
            Map<String, VisitCountry> existingCountryMap = new HashMap<>();

            try {
                existingCountries = visitCountryRepository.findByVideo(video);
                for (VisitCountry existing : existingCountries) {
                    if (existing != null && existing.getCountryCode() != null) {
                        existingCountryMap.put(existing.getCountryCode(), existing);
                    }
                }
            } catch (Exception e) {
                logger.warn("기존 국가 정보 조회 실패, 새로 생성: {} - {}", video.getVideoId(), e.getMessage());
                existingCountries = new ArrayList<>();
            }

            if (detectedLocations == null || detectedLocations.isEmpty()) {
                logger.warn("감지된 국가 정보가 없습니다: {}", video.getVideoId());
                return savedCountries;
            }

            for (DetectedLocation location : detectedLocations) {
                try {
                    CountryInfo countryInfo = location != null ? location.getCountry() : null;

                    // CountryInfo 검증
                    if (countryInfo == null || countryInfo.getCode() == null || countryInfo.getName() == null) {
                        logger.warn("유효하지 않은 CountryInfo, 건너뜀: {}",
                                countryInfo != null ? countryInfo.getCode() : "null");
                        continue;
                    }

                    VisitCountry visitCountry = existingCountryMap.get(countryInfo.getCode());

                    if (visitCountry != null) {
                        // 기존 데이터가 있으면 업데이트
                        try {
                            boolean updated = false;

                            if (!Objects.equals(visitCountry.getCountryName(), countryInfo.getName())) {
                                visitCountry.setCountryName(countryInfo.getName());
                                updated = true;
                            }

                            if (!Objects.equals(visitCountry.getCountryEmoji(), countryInfo.getEmoji())) {
                                visitCountry.setCountryEmoji(countryInfo.getEmoji());
                                updated = true;
                            }

                            if (!Objects.equals(visitCountry.getContinent(), countryInfo.getContinent())) {
                                visitCountry.setContinent(countryInfo.getContinent());
                                updated = true;
                            }

                            if (!Objects.equals(visitCountry.getCityName(), location.getCityName())) {
                                visitCountry.setCityName(location.getCityName());
                                visitCountry.setCityLatitude(location.getCityLatitude());
                                visitCountry.setCityLongitude(location.getCityLongitude());
                                updated = true;
                            }

                            if (updated) {
                                visitCountry.setDetectionMethod("TITLE_UPDATE");
                                visitCountry.setConfidenceScore(0.8);
                                visitCountry = visitCountryRepository.save(visitCountry);
                                logger.debug("국가 정보 업데이트: {} ({})", countryInfo.getName(), countryInfo.getCode());
                            }

                            savedCountries.add(visitCountry);
                        } catch (Exception e) {
                            logger.warn("기존 국가 정보 업데이트 실패, 건너뜀: {} - {}",
                                    countryInfo.getCode(), e.getMessage());
                            continue;
                        }
                    } else {
                        // 새로운 데이터 생성
                        try {
                            boolean isDefault = countryInfo.getCode().equals("KR")
                                    && detectedLocations.size() == 1
                                    && detectedLocations.get(0).getCountry() == DEFAULT_COUNTRY;

                            visitCountry = new VisitCountry();
                            visitCountry.setVideo(video);
                            visitCountry.setCountryCode(countryInfo.getCode());
                            visitCountry.setCountryName(countryInfo.getName());
                            visitCountry.setCountryEmoji(countryInfo.getEmoji());
                            visitCountry.setContinent(countryInfo.getContinent());
                            visitCountry.setCityName(location.getCityName());
                            visitCountry.setCityLatitude(location.getCityLatitude());
                            visitCountry.setCityLongitude(location.getCityLongitude());
                            visitCountry.setDetectionMethod(isDefault ? "DEFAULT" : location.getDetectionMethod());
                            visitCountry.setConfidenceScore(isDefault ? 0.5 : 0.7);

                            // 저장 전 최종 검증
                            if (visitCountry.getCountryCode() == null || visitCountry.getCountryName() == null) {
                                logger.warn("VisitCountry 필수 필드가 null, 건너뜀: countryCode={}, countryName={}",
                                        visitCountry.getCountryCode(), visitCountry.getCountryName());
                                continue;
                            }

                            visitCountry = visitCountryRepository.save(visitCountry);
                            savedCountries.add(visitCountry);

                            logger.debug("새 국가 정보 저장: {} ({}){}", countryInfo.getName(), countryInfo.getCode(),
                                    location.getCityName() != null ? " - " + location.getCityName() : "");
                        } catch (Exception e) {
                            logger.warn("새 국가 정보 저장 실패, 건너뜀: {} - {}",
                                    countryInfo.getCode(), e.getMessage());
                            continue;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("국가 정보 처리 실패, 건너뜀: {} - {}",
                            location != null && location.getCountry() != null ? location.getCountry().getCode() : "unknown", e.getMessage());
                    // 개별 국가 저장 실패해도 계속 진행
                    continue;
                }
            }

            // 이번에 더 이상 감지되지 않는 기존 국가 정보는 정리한다.
            // (키워드를 새로 추가해서 재처리했을 때, 예전에 잘못 붙은 기본값(대한민국) 등이
            // 새로 감지된 국가와 중복으로 계속 남아있지 않도록 한다)
            try {
                Set<String> detectedCodes = new HashSet<>();
                for (DetectedLocation location : detectedLocations) {
                    if (location != null && location.getCountry() != null && location.getCountry().getCode() != null) {
                        detectedCodes.add(location.getCountry().getCode());
                    }
                }

                for (Map.Entry<String, VisitCountry> entry : existingCountryMap.entrySet()) {
                    if (!detectedCodes.contains(entry.getKey())) {
                        try {
                            visitCountryRepository.delete(entry.getValue());
                            logger.debug("더 이상 감지되지 않는 국가 정보 삭제: {} ({})",
                                    entry.getValue().getCountryName(), entry.getKey());
                        } catch (Exception e) {
                            logger.warn("오래된 국가 정보 삭제 실패: {} - {}", entry.getKey(), e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("오래된 국가 정보 정리 실패: {} - {}", video.getVideoId(), e.getMessage());
            }
        } catch (Exception e) {
            logger.error("국가 정보 저장 과정에서 전체 오류 발생: {} - {}",
                    video != null ? video.getVideoId() : "unknown", e.getMessage());
        }

        return savedCountries;
    }

    /**
     * 기본 국가(대한민국) 저장
     */
    private List<VisitCountry> saveDefaultCountry(Video video) {
        try {
            return saveOrUpdateCountries(video, Collections.singletonList(new DetectedLocation(DEFAULT_COUNTRY)));
        } catch (Exception e) {
            logger.error("기본 국가 저장 실패: {} - {}", video.getVideoId(), e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 국기 이모지 매핑 초기화
     */
    private static void initializeFlagEmojiMap() {
        FLAG_EMOJI_MAP.put("🇰🇷", new CountryInfo("KR", "대한민국", "Asia", "🇰🇷"));
        FLAG_EMOJI_MAP.put("🇯🇵", new CountryInfo("JP", "일본", "Asia", "🇯🇵"));
        FLAG_EMOJI_MAP.put("🇨🇳", new CountryInfo("CN", "중국", "Asia", "🇨🇳"));
        FLAG_EMOJI_MAP.put("🇺🇸", new CountryInfo("US", "미국", "North America", "🇺🇸"));
        FLAG_EMOJI_MAP.put("🇫🇷", new CountryInfo("FR", "프랑스", "Europe", "🇫🇷"));
        FLAG_EMOJI_MAP.put("🇬🇧", new CountryInfo("GB", "영국", "Europe", "🇬🇧"));
        FLAG_EMOJI_MAP.put("🇩🇪", new CountryInfo("DE", "독일", "Europe", "🇩🇪"));
        FLAG_EMOJI_MAP.put("🇮🇹", new CountryInfo("IT", "이탈리아", "Europe", "🇮🇹"));
        FLAG_EMOJI_MAP.put("🇪🇸", new CountryInfo("ES", "스페인", "Europe", "🇪🇸"));
        FLAG_EMOJI_MAP.put("🇦🇺", new CountryInfo("AU", "호주", "Oceania", "🇦🇺"));
        FLAG_EMOJI_MAP.put("🇨🇦", new CountryInfo("CA", "캐나다", "North America", "🇨🇦"));
        FLAG_EMOJI_MAP.put("🇹🇭", new CountryInfo("TH", "태국", "Asia", "🇹🇭"));
        FLAG_EMOJI_MAP.put("🇻🇳", new CountryInfo("VN", "베트남", "Asia", "🇻🇳"));
        FLAG_EMOJI_MAP.put("🇸🇬", new CountryInfo("SG", "싱가포르", "Asia", "🇸🇬"));
        FLAG_EMOJI_MAP.put("🇲🇾", new CountryInfo("MY", "말레이시아", "Asia", "🇲🇾"));
        FLAG_EMOJI_MAP.put("🇮🇩", new CountryInfo("ID", "인도네시아", "Asia", "🇮🇩"));
        FLAG_EMOJI_MAP.put("🇵🇭", new CountryInfo("PH", "필리핀", "Asia", "🇵🇭"));
        FLAG_EMOJI_MAP.put("🇮🇳", new CountryInfo("IN", "인도", "Asia", "🇮🇳"));
        FLAG_EMOJI_MAP.put("🇲🇳", new CountryInfo("MN", "몽골", "Asia", "🇲🇳"));
        FLAG_EMOJI_MAP.put("🇷🇺", new CountryInfo("RU", "러시아", "Europe", "🇷🇺"));
        FLAG_EMOJI_MAP.put("🇧🇷", new CountryInfo("BR", "브라질", "South America", "🇧🇷"));
    }

    /**
     * 국가 정보 클래스
     */
    public static class CountryInfo {
        private final String code;
        private final String name;
        private final String continent;
        private final String emoji;

        public CountryInfo(String code, String name, String continent, String emoji) {
            this.code = code;
            this.name = name;
            this.continent = continent;
            this.emoji = emoji;
        }

        public String getCode() { return code; }
        public String getName() { return name; }
        public String getContinent() { return continent; }
        public String getEmoji() { return emoji; }
    }

    /**
     * 제목에서 감지된 위치 하나(국가 + 있으면 도시)를 나타낸다.
     */
    public static class DetectedLocation {
        private final CountryInfo country;
        private final String cityName;
        private final Double cityLatitude;
        private final Double cityLongitude;
        private final String detectionMethod;

        public DetectedLocation(CountryInfo country) {
            this(country, null, null, null);
        }

        public DetectedLocation(CountryInfo country, String cityName, Double cityLatitude, Double cityLongitude) {
            this(country, cityName, cityLatitude, cityLongitude, "TITLE_KEYWORD");
        }

        public DetectedLocation(CountryInfo country, String cityName, Double cityLatitude, Double cityLongitude,
                                 String detectionMethod) {
            this.country = country;
            this.cityName = cityName;
            this.cityLatitude = cityLatitude;
            this.cityLongitude = cityLongitude;
            this.detectionMethod = detectionMethod;
        }

        public CountryInfo getCountry() { return country; }
        public String getCityName() { return cityName; }
        public Double getCityLatitude() { return cityLatitude; }
        public Double getCityLongitude() { return cityLongitude; }
        public String getDetectionMethod() { return detectionMethod; }
    }
}
