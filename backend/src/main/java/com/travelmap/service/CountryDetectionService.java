package com.travelmap.backend.service;

import com.travelmap.backend.entity.Video;
import com.travelmap.backend.entity.VisitCountry;
import com.travelmap.backend.repository.VisitCountryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class CountryDetectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(CountryDetectionService.class);
    
    private final VisitCountryRepository visitCountryRepository;
    
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
    public CountryDetectionService(VisitCountryRepository visitCountryRepository) {
        this.visitCountryRepository = visitCountryRepository;
    }
    
    /**
     * 영상 제목에서 국가 정보를 추출
     */
    public List<VisitCountry> extractCountriesFromTitle(Video video) {
        logger.info("영상 제목에서 국가 추출 시작: {}", video.getTitle());
        
        List<VisitCountry> countries = new ArrayList<>();
        
        // 1. 국기 이모지로 국가 탐지
        countries.addAll(detectCountriesByFlagEmoji(video));
        
        // 2. 국가명 키워드로 국가 탐지
        countries.addAll(detectCountriesByKeywords(video));
        
        // 중복 제거
        countries = removeDuplicateCountries(countries);
        
        // 데이터베이스에 저장
        List<VisitCountry> savedCountries = new ArrayList<>();
        for (VisitCountry country : countries) {
            VisitCountry savedCountry = visitCountryRepository.save(country);
            savedCountries.add(savedCountry);
        }
        
        logger.info("영상에서 {}개 국가 추출 완료", savedCountries.size());
        
        return savedCountries;
    }
    
    /**
     * 국기 이모지로 국가 탐지
     */
    private List<VisitCountry> detectCountriesByFlagEmoji(Video video) {
        List<VisitCountry> countries = new ArrayList<>();
        String title = video.getTitle();
        
        for (Map.Entry<String, CountryInfo> entry : FLAG_EMOJI_MAP.entrySet()) {
            if (title.contains(entry.getKey())) {
                CountryInfo countryInfo = entry.getValue();
                
                VisitCountry visitCountry = new VisitCountry();
                visitCountry.setVideo(video);
                visitCountry.setCountryCode(countryInfo.getCode());
                visitCountry.setCountryName(countryInfo.getName());
                visitCountry.setCountryEmoji(entry.getKey());
                visitCountry.setContinent(countryInfo.getContinent());
                visitCountry.setDetectionMethod("TITLE_EMOJI");
                visitCountry.setConfidenceScore(0.9);
                
                countries.add(visitCountry);
                
                logger.debug("국기 이모지로 국가 탐지: {} ({})", countryInfo.getName(), entry.getKey());
            }
        }
        
        return countries;
    }
    
    /**
     * 국가명 키워드로 국가 탐지
     */
    private List<VisitCountry> detectCountriesByKeywords(Video video) {
        List<VisitCountry> countries = new ArrayList<>();
        String title = video.getTitle().toLowerCase();
        
        for (Map.Entry<String, CountryInfo> entry : COUNTRY_KEYWORDS.entrySet()) {
            String keyword = entry.getKey().toLowerCase();
            
            if (title.contains(keyword)) {
                CountryInfo countryInfo = entry.getValue();
                
                VisitCountry visitCountry = new VisitCountry();
                visitCountry.setVideo(video);
                visitCountry.setCountryCode(countryInfo.getCode());
                visitCountry.setCountryName(countryInfo.getName());
                visitCountry.setContinent(countryInfo.getContinent());
                visitCountry.setDetectionMethod("TITLE_KEYWORD");
                visitCountry.setConfidenceScore(0.7);
                
                countries.add(visitCountry);
                
                logger.debug("키워드로 국가 탐지: {} (키워드: {})", countryInfo.getName(), keyword);
            }
        }
        
        return countries;
    }
    
    /**
     * 중복 국가 제거
     */
    private List<VisitCountry> removeDuplicateCountries(List<VisitCountry> countries) {
        Map<String, VisitCountry> uniqueCountries = new LinkedHashMap<>();
        
        for (VisitCountry country : countries) {
            String key = country.getCountryCode();
            VisitCountry existing = uniqueCountries.get(key);
            
            // 이미 존재하는 국가가 있으면 신뢰도가 높은 것을 선택
            if (existing == null || country.getConfidenceScore() > existing.getConfidenceScore()) {
                uniqueCountries.put(key, country);
            }
        }
        
        return new ArrayList<>(uniqueCountries.values());
    }
    
    /**
     * 국기 이모지 매핑 초기화
     */
    private static void initializeFlagEmojiMap() {
        FLAG_EMOJI_MAP.put("🇰🇷", new CountryInfo("KR", "대한민국", "Asia"));
        FLAG_EMOJI_MAP.put("🇯🇵", new CountryInfo("JP", "일본", "Asia"));
        FLAG_EMOJI_MAP.put("🇨🇳", new CountryInfo("CN", "중국", "Asia"));
        FLAG_EMOJI_MAP.put("🇺🇸", new CountryInfo("US", "미국", "North America"));
        FLAG_EMOJI_MAP.put("🇫🇷", new CountryInfo("FR", "프랑스", "Europe"));
        FLAG_EMOJI_MAP.put("🇬🇧", new CountryInfo("GB", "영국", "Europe"));
        FLAG_EMOJI_MAP.put("🇩🇪", new CountryInfo("DE", "독일", "Europe"));
        FLAG_EMOJI_MAP.put("🇮🇹", new CountryInfo("IT", "이탈리아", "Europe"));
        FLAG_EMOJI_MAP.put("🇪🇸", new CountryInfo("ES", "스페인", "Europe"));
        FLAG_EMOJI_MAP.put("🇦🇺", new CountryInfo("AU", "호주", "Oceania"));
        FLAG_EMOJI_MAP.put("🇨🇦", new CountryInfo("CA", "캐나다", "North America"));
        FLAG_EMOJI_MAP.put("🇹🇭", new CountryInfo("TH", "태국", "Asia"));
        FLAG_EMOJI_MAP.put("🇻🇳", new CountryInfo("VN", "베트남", "Asia"));
        FLAG_EMOJI_MAP.put("🇸🇬", new CountryInfo("SG", "싱가포르", "Asia"));
        FLAG_EMOJI_MAP.put("🇲🇾", new CountryInfo("MY", "말레이시아", "Asia"));
        FLAG_EMOJI_MAP.put("🇮🇩", new CountryInfo("ID", "인도네시아", "Asia"));
        FLAG_EMOJI_MAP.put("🇵🇭", new CountryInfo("PH", "필리핀", "Asia"));
        FLAG_EMOJI_MAP.put("🇮🇳", new CountryInfo("IN", "인도", "Asia"));
        FLAG_EMOJI_MAP.put("🇷🇺", new CountryInfo("RU", "러시아", "Europe"));
        FLAG_EMOJI_MAP.put("🇧🇷", new CountryInfo("BR", "브라질", "South America"));
    }
    
    /**
     * 국가명 키워드 매핑 초기화
     */
    private static void initializeCountryKeywords() {
        // 한국
        COUNTRY_KEYWORDS.put("한국", new CountryInfo("KR", "대한민국", "Asia"));
        COUNTRY_KEYWORDS.put("대한민국", new CountryInfo("KR", "대한민국", "Asia"));
        COUNTRY_KEYWORDS.put("korea", new CountryInfo("KR", "대한민국", "Asia"));
        COUNTRY_KEYWORDS.put("seoul", new CountryInfo("KR", "대한민국", "Asia"));
        COUNTRY_KEYWORDS.put("서울", new CountryInfo("KR", "대한민국", "Asia"));
        
        // 일본
        COUNTRY_KEYWORDS.put("일본", new CountryInfo("JP", "일본", "Asia"));
        COUNTRY_KEYWORDS.put("japan", new CountryInfo("JP", "일본", "Asia"));
        COUNTRY_KEYWORDS.put("tokyo", new CountryInfo("JP", "일본", "Asia"));
        COUNTRY_KEYWORDS.put("도쿄", new CountryInfo("JP", "일본", "Asia"));
        COUNTRY_KEYWORDS.put("오사카", new CountryInfo("JP", "일본", "Asia"));
        COUNTRY_KEYWORDS.put("osaka", new CountryInfo("JP", "일본", "Asia"));
        
        // 중국
        COUNTRY_KEYWORDS.put("중국", new CountryInfo("CN", "중국", "Asia"));
        COUNTRY_KEYWORDS.put("china", new CountryInfo("CN", "중국", "Asia"));
        COUNTRY_KEYWORDS.put("beijing", new CountryInfo("CN", "중국", "Asia"));
        COUNTRY_KEYWORDS.put("베이징", new CountryInfo("CN", "중국", "Asia"));
        COUNTRY_KEYWORDS.put("상하이", new CountryInfo("CN", "중국", "Asia"));
        COUNTRY_KEYWORDS.put("shanghai", new CountryInfo("CN", "중국", "Asia"));
        
        // 미국
        COUNTRY_KEYWORDS.put("미국", new CountryInfo("US", "미국", "North America"));
        COUNTRY_KEYWORDS.put("america", new CountryInfo("US", "미국", "North America"));
        COUNTRY_KEYWORDS.put("usa", new CountryInfo("US", "미국", "North America"));
        COUNTRY_KEYWORDS.put("new york", new CountryInfo("US", "미국", "North America"));
        COUNTRY_KEYWORDS.put("뉴욕", new CountryInfo("US", "미국", "North America"));
        
        // 프랑스
        COUNTRY_KEYWORDS.put("프랑스", new CountryInfo("FR", "프랑스", "Europe"));
        COUNTRY_KEYWORDS.put("france", new CountryInfo("FR", "프랑스", "Europe"));
        COUNTRY_KEYWORDS.put("paris", new CountryInfo("FR", "프랑스", "Europe"));
        COUNTRY_KEYWORDS.put("파리", new CountryInfo("FR", "프랑스", "Europe"));
        
        // 태국
        COUNTRY_KEYWORDS.put("태국", new CountryInfo("TH", "태국", "Asia"));
        COUNTRY_KEYWORDS.put("thailand", new CountryInfo("TH", "태국", "Asia"));
        COUNTRY_KEYWORDS.put("bangkok", new CountryInfo("TH", "태국", "Asia"));
        COUNTRY_KEYWORDS.put("방콕", new CountryInfo("TH", "태국", "Asia"));
    }
    
    /**
     * 국가 정보 클래스
     */
    public static class CountryInfo {
        private final String code;
        private final String name;
        private final String continent;
        
        public CountryInfo(String code, String name, String continent) {
            this.code = code;
            this.name = name;
            this.continent = continent;
        }
        
        public String getCode() { return code; }
        public String getName() { return name; }
        public String getContinent() { return continent; }
    }
} 