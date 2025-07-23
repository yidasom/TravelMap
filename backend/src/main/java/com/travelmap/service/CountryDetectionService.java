package com.travelmap.service;

import com.travelmap.entity.Video;
import com.travelmap.entity.VisitCountry;
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
    
    // 대한민국 기본 정보
    private static final CountryInfo DEFAULT_COUNTRY = new CountryInfo("KR", "대한민국", "Asia", "🇰🇷");
    
    // 국기 이모지와 국가 코드/이름 매핑
    private static final Map<String, CountryInfo> FLAG_EMOJI_MAP = new HashMap<>();
    
    // 국가명 키워드 매핑 (한국어, 영어)
    private static final Map<String, CountryInfo> COUNTRY_KEYWORDS = new HashMap<>();
    
    static {
        // 주요 국가들의 이모지 매핑 설정
        initializeFlagEmojiMap();
        initializeCountryKeywords();
    }
    
    @Autowired
    public CountryDetectionService(VisitCountryRepository visitCountryRepository, VideoRepository videoRepository) {
        this.visitCountryRepository = visitCountryRepository;
        this.videoRepository = videoRepository;
    }
    
    /**
     * 영상 제목에서 국가 정보를 추출하고 저장
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
            
            List<CountryInfo> detectedCountries = new ArrayList<>();
            
            // 1. 국기 이모지로 국가 탐지 (예외 처리)
            try {
                detectedCountries.addAll(detectCountriesByFlagEmoji(persistentVideo.getTitle()));
            } catch (Exception e) {
                logger.warn("국기 이모지 탐지 실패: {} - {}", persistentVideo.getVideoId(), e.getMessage());
            }
            
            // 2. 국가명 키워드로 국가 탐지 (예외 처리)
            try {
                detectedCountries.addAll(detectCountriesByKeywords(persistentVideo.getTitle()));
            } catch (Exception e) {
                logger.warn("키워드 탐지 실패: {} - {}", persistentVideo.getVideoId(), e.getMessage());
            }
            
            // 중복 제거 (예외 처리)
            try {
                detectedCountries = removeDuplicateDetectedCountries(detectedCountries);
            } catch (Exception e) {
                logger.warn("중복 제거 실패: {} - {}", persistentVideo.getVideoId(), e.getMessage());
                // 중복 제거 실패해도 계속 진행
            }
            
            // 3. 국가를 찾지 못한 경우 기본값(대한민국) 사용
            if (detectedCountries.isEmpty()) {
                logger.info("제목에서 국가를 찾지 못해 기본값 사용: {}", persistentVideo.getVideoId());
                detectedCountries.add(DEFAULT_COUNTRY);
            }
            
            // 4. 데이터베이스에 저장 또는 업데이트 (예외 처리)
            try {
                savedCountries = saveOrUpdateCountries(persistentVideo, detectedCountries);
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
     * 국기 이모지로 국가 탐지
     */
    private List<CountryInfo> detectCountriesByFlagEmoji(String title) {
        List<CountryInfo> countries = new ArrayList<>();
        
        try {
            if (title == null || title.trim().isEmpty()) {
                return countries;
            }
            
            for (Map.Entry<String, CountryInfo> entry : FLAG_EMOJI_MAP.entrySet()) {
                try {
                    if (title.contains(entry.getKey())) {
                        countries.add(entry.getValue());
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
        
        return countries;
    }
    
    /**
     * 국가명 키워드로 국가 탐지
     */
    private List<CountryInfo> detectCountriesByKeywords(String title) {
        List<CountryInfo> countries = new ArrayList<>();
        
        try {
            if (title == null || title.trim().isEmpty()) {
                return countries;
            }
            
            String titleLower = title.toLowerCase();
            
            for (Map.Entry<String, CountryInfo> entry : COUNTRY_KEYWORDS.entrySet()) {
                try {
                    String keyword = entry.getKey().toLowerCase();
                    
                    if (titleLower.contains(keyword)) {
                        countries.add(entry.getValue());
                        logger.debug("키워드로 국가 탐지: {} (키워드: {})", entry.getValue().getName(), keyword);
                    }
                } catch (Exception e) {
                    logger.warn("개별 키워드 탐지 실패: {} - {}", entry.getKey(), e.getMessage());
                    // 개별 키워드 탐지 실패해도 계속 진행
                    continue;
                }
            }
        } catch (Exception e) {
            logger.warn("키워드 탐지 전체 실패: {}", e.getMessage());
        }
        
        return countries;
    }
    
    /**
     * 중복 국가 제거 (감지된 CountryInfo 리스트에서)
     */
    private List<CountryInfo> removeDuplicateDetectedCountries(List<CountryInfo> countries) {
        try {
            if (countries == null || countries.isEmpty()) {
                return new ArrayList<>();
            }
            
            Map<String, CountryInfo> uniqueCountries = new LinkedHashMap<>();
            
            for (CountryInfo country : countries) {
                try {
                    if (country != null && country.getCode() != null) {
                        uniqueCountries.put(country.getCode(), country);
                    }
                } catch (Exception e) {
                    logger.warn("개별 국가 중복 제거 실패: {} - {}", 
                            country != null ? country.getCode() : "null", e.getMessage());
                    // 개별 국가 처리 실패해도 계속 진행
                    continue;
                }
            }
            
            return new ArrayList<>(uniqueCountries.values());
        } catch (Exception e) {
            logger.warn("중복 제거 전체 실패: {}", e.getMessage());
            // 중복 제거 실패 시 원본 리스트 반환
            return countries != null ? countries : new ArrayList<>();
        }
    }
    
    /**
     * 국가 정보를 데이터베이스에 저장하거나 업데이트
     */
    private List<VisitCountry> saveOrUpdateCountries(Video video, List<CountryInfo> detectedCountries) {
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
            
            if (detectedCountries == null || detectedCountries.isEmpty()) {
                logger.warn("감지된 국가 정보가 없습니다: {}", video.getVideoId());
                return savedCountries;
            }
            
            for (CountryInfo countryInfo : detectedCountries) {
                try {
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
                            visitCountry = new VisitCountry();
                            visitCountry.setVideo(video);
                            visitCountry.setCountryCode(countryInfo.getCode());
                            visitCountry.setCountryName(countryInfo.getName());
                            visitCountry.setCountryEmoji(countryInfo.getEmoji());
                            visitCountry.setContinent(countryInfo.getContinent());
                            visitCountry.setDetectionMethod(countryInfo.getCode().equals("KR") && detectedCountries.size() == 1 && detectedCountries.get(0) == DEFAULT_COUNTRY ? "DEFAULT" : "TITLE_KEYWORD");
                            visitCountry.setConfidenceScore(countryInfo.getCode().equals("KR") && detectedCountries.size() == 1 && detectedCountries.get(0) == DEFAULT_COUNTRY ? 0.5 : 0.7);
                            
                            // 저장 전 최종 검증
                            if (visitCountry.getCountryCode() == null || visitCountry.getCountryName() == null) {
                                logger.warn("VisitCountry 필수 필드가 null, 건너뜀: countryCode={}, countryName={}", 
                                        visitCountry.getCountryCode(), visitCountry.getCountryName());
                                continue;
                            }
                            
                            visitCountry = visitCountryRepository.save(visitCountry);
                            savedCountries.add(visitCountry);
                            
                            logger.debug("새 국가 정보 저장: {} ({})", countryInfo.getName(), countryInfo.getCode());
                        } catch (Exception e) {
                            logger.warn("새 국가 정보 저장 실패, 건너뜀: {} - {}", 
                                    countryInfo.getCode(), e.getMessage());
                            continue;
                        }
                    }
                } catch (Exception e) {
                    logger.warn("국가 정보 처리 실패, 건너뜀: {} - {}", 
                            countryInfo != null ? countryInfo.getCode() : "unknown", e.getMessage());
                    // 개별 국가 저장 실패해도 계속 진행
                    continue;
                }
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
            return saveOrUpdateCountries(video, Collections.singletonList(DEFAULT_COUNTRY));
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
     * 국가명 키워드 매핑 초기화
     */
    private static void initializeCountryKeywords() {
        // 한국
        COUNTRY_KEYWORDS.put("한국", new CountryInfo("KR", "대한민국", "Asia", "🇰🇷"));
        COUNTRY_KEYWORDS.put("대한민국", new CountryInfo("KR", "대한민국", "Asia", "🇰🇷"));
        COUNTRY_KEYWORDS.put("korea", new CountryInfo("KR", "대한민국", "Asia", "🇰🇷"));
        COUNTRY_KEYWORDS.put("seoul", new CountryInfo("KR", "대한민국", "Asia", "🇰🇷"));
        COUNTRY_KEYWORDS.put("서울", new CountryInfo("KR", "대한민국", "Asia", "🇰🇷"));
        
        // 일본
        COUNTRY_KEYWORDS.put("일본", new CountryInfo("JP", "일본", "Asia", "🇯🇵"));
        COUNTRY_KEYWORDS.put("japan", new CountryInfo("JP", "일본", "Asia", "🇯🇵"));
        COUNTRY_KEYWORDS.put("tokyo", new CountryInfo("JP", "일본", "Asia", "🇯🇵"));
        COUNTRY_KEYWORDS.put("도쿄", new CountryInfo("JP", "일본", "Asia", "🇯🇵"));
        COUNTRY_KEYWORDS.put("오사카", new CountryInfo("JP", "일본", "Asia", "🇯🇵"));
        COUNTRY_KEYWORDS.put("osaka", new CountryInfo("JP", "일본", "Asia", "🇯🇵"));
        
        // 중국
        COUNTRY_KEYWORDS.put("중국", new CountryInfo("CN", "중국", "Asia", "🇨🇳"));
        COUNTRY_KEYWORDS.put("china", new CountryInfo("CN", "중국", "Asia", "🇨🇳"));
        COUNTRY_KEYWORDS.put("beijing", new CountryInfo("CN", "중국", "Asia", "🇨🇳"));
        COUNTRY_KEYWORDS.put("베이징", new CountryInfo("CN", "중국", "Asia", "🇨🇳"));
        COUNTRY_KEYWORDS.put("상하이", new CountryInfo("CN", "중국", "Asia", "🇨🇳"));
        COUNTRY_KEYWORDS.put("shanghai", new CountryInfo("CN", "중국", "Asia", "🇨🇳"));
        
        // 미국
        COUNTRY_KEYWORDS.put("미국", new CountryInfo("US", "미국", "North America", "🇺🇸"));
        COUNTRY_KEYWORDS.put("america", new CountryInfo("US", "미국", "North America", "🇺🇸"));
        COUNTRY_KEYWORDS.put("usa", new CountryInfo("US", "미국", "North America", "🇺🇸"));
        COUNTRY_KEYWORDS.put("new york", new CountryInfo("US", "미국", "North America", "🇺🇸"));
        COUNTRY_KEYWORDS.put("뉴욕", new CountryInfo("US", "미국", "North America", "🇺🇸"));
        
        // 프랑스
        COUNTRY_KEYWORDS.put("프랑스", new CountryInfo("FR", "프랑스", "Europe", "🇫🇷"));
        COUNTRY_KEYWORDS.put("france", new CountryInfo("FR", "프랑스", "Europe", "🇫🇷"));
        COUNTRY_KEYWORDS.put("paris", new CountryInfo("FR", "프랑스", "Europe", "🇫🇷"));
        COUNTRY_KEYWORDS.put("파리", new CountryInfo("FR", "프랑스", "Europe", "🇫🇷"));
        
        // 태국
        COUNTRY_KEYWORDS.put("태국", new CountryInfo("TH", "태국", "Asia", "🇹🇭"));
        COUNTRY_KEYWORDS.put("thailand", new CountryInfo("TH", "태국", "Asia", "🇹🇭"));
        COUNTRY_KEYWORDS.put("bangkok", new CountryInfo("TH", "태국", "Asia", "🇹🇭"));
        COUNTRY_KEYWORDS.put("방콕", new CountryInfo("TH", "태국", "Asia", "🇹🇭"));

        // 몽골
        COUNTRY_KEYWORDS.put("몽골", new CountryInfo("MN", "몽골", "Asia", "🇲🇳"));
        COUNTRY_KEYWORDS.put("mongolia", new CountryInfo("MN", "몽골", "Asia", "🇲🇳"));
        COUNTRY_KEYWORDS.put("mongolian", new CountryInfo("MN", "몽골", "Asia", "🇲🇳"));
        COUNTRY_KEYWORDS.put("ulaanbaatar", new CountryInfo("MN", "몽골", "Asia", "🇲🇳"));
        COUNTRY_KEYWORDS.put("울란바토르", new CountryInfo("MN", "몽골", "Asia", "🇲🇳"));
        
        // 영국
        COUNTRY_KEYWORDS.put("영국", new CountryInfo("GB", "영국", "Europe", "🇬🇧"));
        COUNTRY_KEYWORDS.put("britain", new CountryInfo("GB", "영국", "Europe", "🇬🇧"));
        COUNTRY_KEYWORDS.put("uk", new CountryInfo("GB", "영국", "Europe", "🇬🇧"));
        COUNTRY_KEYWORDS.put("london", new CountryInfo("GB", "영국", "Europe", "🇬🇧"));
        COUNTRY_KEYWORDS.put("런던", new CountryInfo("GB", "영국", "Europe", "🇬🇧"));
        
        // 독일
        COUNTRY_KEYWORDS.put("독일", new CountryInfo("DE", "독일", "Europe", "🇩🇪"));
        COUNTRY_KEYWORDS.put("germany", new CountryInfo("DE", "독일", "Europe", "🇩🇪"));
        COUNTRY_KEYWORDS.put("berlin", new CountryInfo("DE", "독일", "Europe", "🇩🇪"));
        COUNTRY_KEYWORDS.put("베를린", new CountryInfo("DE", "독일", "Europe", "🇩🇪"));
        
        // 이탈리아
        COUNTRY_KEYWORDS.put("이탈리아", new CountryInfo("IT", "이탈리아", "Europe", "🇮🇹"));
        COUNTRY_KEYWORDS.put("italy", new CountryInfo("IT", "이탈리아", "Europe", "🇮🇹"));
        COUNTRY_KEYWORDS.put("rome", new CountryInfo("IT", "이탈리아", "Europe", "🇮🇹"));
        COUNTRY_KEYWORDS.put("로마", new CountryInfo("IT", "이탈리아", "Europe", "🇮🇹"));
        
        // 스페인
        COUNTRY_KEYWORDS.put("스페인", new CountryInfo("ES", "스페인", "Europe", "🇪🇸"));
        COUNTRY_KEYWORDS.put("spain", new CountryInfo("ES", "스페인", "Europe", "🇪🇸"));
        COUNTRY_KEYWORDS.put("madrid", new CountryInfo("ES", "스페인", "Europe", "🇪🇸"));
        COUNTRY_KEYWORDS.put("마드리드", new CountryInfo("ES", "스페인", "Europe", "🇪🇸"));
        
        // 호주
        COUNTRY_KEYWORDS.put("호주", new CountryInfo("AU", "호주", "Oceania", "🇦🇺"));
        COUNTRY_KEYWORDS.put("australia", new CountryInfo("AU", "호주", "Oceania", "🇦🇺"));
        COUNTRY_KEYWORDS.put("sydney", new CountryInfo("AU", "호주", "Oceania", "🇦🇺"));
        COUNTRY_KEYWORDS.put("시드니", new CountryInfo("AU", "호주", "Oceania", "🇦🇺"));
        
        // 캐나다
        COUNTRY_KEYWORDS.put("캐나다", new CountryInfo("CA", "캐나다", "North America", "🇨🇦"));
        COUNTRY_KEYWORDS.put("canada", new CountryInfo("CA", "캐나다", "North America", "🇨🇦"));
        COUNTRY_KEYWORDS.put("toronto", new CountryInfo("CA", "캐나다", "North America", "🇨🇦"));
        COUNTRY_KEYWORDS.put("토론토", new CountryInfo("CA", "캐나다", "North America", "🇨🇦"));
        
        // 베트남
        COUNTRY_KEYWORDS.put("베트남", new CountryInfo("VN", "베트남", "Asia", "🇻🇳"));
        COUNTRY_KEYWORDS.put("vietnam", new CountryInfo("VN", "베트남", "Asia", "🇻🇳"));
        COUNTRY_KEYWORDS.put("ho chi minh", new CountryInfo("VN", "베트남", "Asia", "🇻🇳"));
        COUNTRY_KEYWORDS.put("호치민", new CountryInfo("VN", "베트남", "Asia", "🇻🇳"));
        
        // 싱가포르
        COUNTRY_KEYWORDS.put("싱가포르", new CountryInfo("SG", "싱가포르", "Asia", "🇸🇬"));
        COUNTRY_KEYWORDS.put("singapore", new CountryInfo("SG", "싱가포르", "Asia", "🇸🇬"));
        
        // 말레이시아
        COUNTRY_KEYWORDS.put("말레이시아", new CountryInfo("MY", "말레이시아", "Asia", "🇲🇾"));
        COUNTRY_KEYWORDS.put("malaysia", new CountryInfo("MY", "말레이시아", "Asia", "🇲🇾"));
        COUNTRY_KEYWORDS.put("kuala lumpur", new CountryInfo("MY", "말레이시아", "Asia", "🇲🇾"));
        COUNTRY_KEYWORDS.put("쿠알라룸푸르", new CountryInfo("MY", "말레이시아", "Asia", "🇲🇾"));
        
        // 인도네시아
        COUNTRY_KEYWORDS.put("인도네시아", new CountryInfo("ID", "인도네시아", "Asia", "🇮🇩"));
        COUNTRY_KEYWORDS.put("indonesia", new CountryInfo("ID", "인도네시아", "Asia", "🇮🇩"));
        COUNTRY_KEYWORDS.put("jakarta", new CountryInfo("ID", "인도네시아", "Asia", "🇮🇩"));
        COUNTRY_KEYWORDS.put("자카르타", new CountryInfo("ID", "인도네시아", "Asia", "🇮🇩"));
        
        // 필리핀
        COUNTRY_KEYWORDS.put("필리핀", new CountryInfo("PH", "필리핀", "Asia", "🇵🇭"));
        COUNTRY_KEYWORDS.put("philippines", new CountryInfo("PH", "필리핀", "Asia", "🇵🇭"));
        COUNTRY_KEYWORDS.put("manila", new CountryInfo("PH", "필리핀", "Asia", "🇵🇭"));
        COUNTRY_KEYWORDS.put("마닐라", new CountryInfo("PH", "필리핀", "Asia", "🇵🇭"));
        
        // 인도
        COUNTRY_KEYWORDS.put("인도", new CountryInfo("IN", "인도", "Asia", "🇮🇳"));
        COUNTRY_KEYWORDS.put("india", new CountryInfo("IN", "인도", "Asia", "🇮🇳"));
        COUNTRY_KEYWORDS.put("mumbai", new CountryInfo("IN", "인도", "Asia", "🇮🇳"));
        COUNTRY_KEYWORDS.put("뭄바이", new CountryInfo("IN", "인도", "Asia", "🇮🇳"));
        
        // 러시아
        COUNTRY_KEYWORDS.put("러시아", new CountryInfo("RU", "러시아", "Europe", "🇷🇺"));
        COUNTRY_KEYWORDS.put("russia", new CountryInfo("RU", "러시아", "Europe", "🇷🇺"));
        COUNTRY_KEYWORDS.put("moscow", new CountryInfo("RU", "러시아", "Europe", "🇷🇺"));
        COUNTRY_KEYWORDS.put("모스크바", new CountryInfo("RU", "러시아", "Europe", "🇷🇺"));
        
        // 브라질
        COUNTRY_KEYWORDS.put("브라질", new CountryInfo("BR", "브라질", "South America", "🇧🇷"));
        COUNTRY_KEYWORDS.put("brazil", new CountryInfo("BR", "브라질", "South America", "🇧🇷"));
        COUNTRY_KEYWORDS.put("rio", new CountryInfo("BR", "브라질", "South America", "🇧🇷"));
        COUNTRY_KEYWORDS.put("리우", new CountryInfo("BR", "브라질", "South America", "🇧🇷"));
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
} 