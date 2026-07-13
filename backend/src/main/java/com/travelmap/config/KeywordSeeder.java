package com.travelmap.config;

import com.travelmap.entity.CityKeyword;
import com.travelmap.entity.CountryKeyword;
import com.travelmap.repository.CityKeywordRepository;
import com.travelmap.repository.CountryKeywordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 국가/도시 키워드 테이블이 비어있으면 기본 키워드 세트로 채워 넣는다.
 * (예전에는 CountryDetectionService에 하드코딩돼 있던 것을 DB로 옮기면서, 최초 기동 시 1회만 시딩)
 */
@Component
public class KeywordSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(KeywordSeeder.class);

    private final CountryKeywordRepository countryKeywordRepository;
    private final CityKeywordRepository cityKeywordRepository;

    @Autowired
    public KeywordSeeder(CountryKeywordRepository countryKeywordRepository,
                          CityKeywordRepository cityKeywordRepository) {
        this.countryKeywordRepository = countryKeywordRepository;
        this.cityKeywordRepository = cityKeywordRepository;
    }

    @Override
    public void run(String... args) {
        if (countryKeywordRepository.count() == 0) {
            seedCountryKeywords();
        }
        if (cityKeywordRepository.count() == 0) {
            seedCityKeywords();
        }
    }

    private void seedCountryKeywords() {
        List<CountryKeyword> seeds = new ArrayList<>();

        addCountry(seeds, "한국", "KR", "대한민국", "Asia", "🇰🇷");
        addCountry(seeds, "대한민국", "KR", "대한민국", "Asia", "🇰🇷");
        addCountry(seeds, "korea", "KR", "대한민국", "Asia", "🇰🇷");

        addCountry(seeds, "일본", "JP", "일본", "Asia", "🇯🇵");
        addCountry(seeds, "japan", "JP", "일본", "Asia", "🇯🇵");

        addCountry(seeds, "중국", "CN", "중국", "Asia", "🇨🇳");
        addCountry(seeds, "china", "CN", "중국", "Asia", "🇨🇳");

        addCountry(seeds, "미국", "US", "미국", "North America", "🇺🇸");
        addCountry(seeds, "america", "US", "미국", "North America", "🇺🇸");
        addCountry(seeds, "usa", "US", "미국", "North America", "🇺🇸");

        addCountry(seeds, "프랑스", "FR", "프랑스", "Europe", "🇫🇷");
        addCountry(seeds, "france", "FR", "프랑스", "Europe", "🇫🇷");

        addCountry(seeds, "태국", "TH", "태국", "Asia", "🇹🇭");
        addCountry(seeds, "thailand", "TH", "태국", "Asia", "🇹🇭");

        addCountry(seeds, "몽골", "MN", "몽골", "Asia", "🇲🇳");
        addCountry(seeds, "mongolia", "MN", "몽골", "Asia", "🇲🇳");
        addCountry(seeds, "mongolian", "MN", "몽골", "Asia", "🇲🇳");

        addCountry(seeds, "영국", "GB", "영국", "Europe", "🇬🇧");
        addCountry(seeds, "britain", "GB", "영국", "Europe", "🇬🇧");
        addCountry(seeds, "uk", "GB", "영국", "Europe", "🇬🇧");

        addCountry(seeds, "독일", "DE", "독일", "Europe", "🇩🇪");
        addCountry(seeds, "germany", "DE", "독일", "Europe", "🇩🇪");

        addCountry(seeds, "이탈리아", "IT", "이탈리아", "Europe", "🇮🇹");
        addCountry(seeds, "italy", "IT", "이탈리아", "Europe", "🇮🇹");

        addCountry(seeds, "스페인", "ES", "스페인", "Europe", "🇪🇸");
        addCountry(seeds, "spain", "ES", "스페인", "Europe", "🇪🇸");

        addCountry(seeds, "호주", "AU", "호주", "Oceania", "🇦🇺");
        addCountry(seeds, "australia", "AU", "호주", "Oceania", "🇦🇺");

        addCountry(seeds, "캐나다", "CA", "캐나다", "North America", "🇨🇦");
        addCountry(seeds, "canada", "CA", "캐나다", "North America", "🇨🇦");

        addCountry(seeds, "베트남", "VN", "베트남", "Asia", "🇻🇳");
        addCountry(seeds, "vietnam", "VN", "베트남", "Asia", "🇻🇳");

        addCountry(seeds, "싱가포르", "SG", "싱가포르", "Asia", "🇸🇬");
        addCountry(seeds, "singapore", "SG", "싱가포르", "Asia", "🇸🇬");

        addCountry(seeds, "말레이시아", "MY", "말레이시아", "Asia", "🇲🇾");
        addCountry(seeds, "malaysia", "MY", "말레이시아", "Asia", "🇲🇾");

        addCountry(seeds, "인도네시아", "ID", "인도네시아", "Asia", "🇮🇩");
        addCountry(seeds, "indonesia", "ID", "인도네시아", "Asia", "🇮🇩");

        addCountry(seeds, "필리핀", "PH", "필리핀", "Asia", "🇵🇭");
        addCountry(seeds, "philippines", "PH", "필리핀", "Asia", "🇵🇭");

        addCountry(seeds, "인도", "IN", "인도", "Asia", "🇮🇳");
        addCountry(seeds, "india", "IN", "인도", "Asia", "🇮🇳");

        addCountry(seeds, "러시아", "RU", "러시아", "Europe", "🇷🇺");
        addCountry(seeds, "russia", "RU", "러시아", "Europe", "🇷🇺");

        addCountry(seeds, "브라질", "BR", "브라질", "South America", "🇧🇷");
        addCountry(seeds, "brazil", "BR", "브라질", "South America", "🇧🇷");

        // 방글라데시 (누락돼 있던 국가 - 빠니보틀 "여행 난이도 최상급" 시리즈 등에서 발견됨)
        addCountry(seeds, "방글라데시", "BD", "방글라데시", "Asia", "🇧🇩");
        addCountry(seeds, "bangladesh", "BD", "방글라데시", "Asia", "🇧🇩");

        countryKeywordRepository.saveAll(seeds);
        logger.info("국가 키워드 {}개 시딩 완료", seeds.size());
    }

    private void seedCityKeywords() {
        List<CityKeyword> seeds = new ArrayList<>();

        addCity(seeds, "서울", "seoul", "서울", "KR", "대한민국", "Asia", "🇰🇷", 37.5665, 126.9780);
        addCity(seeds, "도쿄", "tokyo", "도쿄", "JP", "일본", "Asia", "🇯🇵", 35.6762, 139.6503);
        addCity(seeds, "오사카", "osaka", "오사카", "JP", "일본", "Asia", "🇯🇵", 34.6937, 135.5023);
        addCity(seeds, "베이징", "beijing", "베이징", "CN", "중국", "Asia", "🇨🇳", 39.9042, 116.4074);
        addCity(seeds, "상하이", "shanghai", "상하이", "CN", "중국", "Asia", "🇨🇳", 31.2304, 121.4737);
        addCity(seeds, "뉴욕", "new york", "뉴욕", "US", "미국", "North America", "🇺🇸", 40.7128, -74.0060);
        addCity(seeds, "엘에이", "la", "로스앤젤레스", "US", "미국", "North America", "🇺🇸", 34.0522, -118.2437);
        addCity(seeds, "파리", "paris", "파리", "FR", "프랑스", "Europe", "🇫🇷", 48.8566, 2.3522);
        addCity(seeds, "방콕", "bangkok", "방콕", "TH", "태국", "Asia", "🇹🇭", 13.7563, 100.5018);
        // 치앙마이 (누락돼 있던 도시 - "원지의 하루" 썸네일에서 발견됨)
        addCity(seeds, "치앙마이", "chiang mai", "치앙마이", "TH", "태국", "Asia", "🇹🇭", 18.7883, 98.9853);
        addCity(seeds, "울란바토르", "ulaanbaatar", "울란바토르", "MN", "몽골", "Asia", "🇲🇳", 47.8864, 106.9057);
        addCity(seeds, "런던", "london", "런던", "GB", "영국", "Europe", "🇬🇧", 51.5074, -0.1278);
        addCity(seeds, "베를린", "berlin", "베를린", "DE", "독일", "Europe", "🇩🇪", 52.5200, 13.4050);
        addCity(seeds, "로마", "rome", "로마", "IT", "이탈리아", "Europe", "🇮🇹", 41.9028, 12.4964);
        addCity(seeds, "마드리드", "madrid", "마드리드", "ES", "스페인", "Europe", "🇪🇸", 40.4168, -3.7038);
        addCity(seeds, "시드니", "sydney", "시드니", "AU", "호주", "Oceania", "🇦🇺", -33.8688, 151.2093);
        addCity(seeds, "토론토", "toronto", "토론토", "CA", "캐나다", "North America", "🇨🇦", 43.6532, -79.3832);
        addCity(seeds, "호치민", "ho chi minh", "호치민", "VN", "베트남", "Asia", "🇻🇳", 10.8231, 106.6297);
        addCity(seeds, "쿠알라룸푸르", "kuala lumpur", "쿠알라룸푸르", "MY", "말레이시아", "Asia", "🇲🇾", 3.1390, 101.6869);
        addCity(seeds, "자카르타", "jakarta", "자카르타", "ID", "인도네시아", "Asia", "🇮🇩", -6.2088, 106.8456);
        addCity(seeds, "마닐라", "manila", "마닐라", "PH", "필리핀", "Asia", "🇵🇭", 14.5995, 120.9842);
        addCity(seeds, "뭄바이", "mumbai", "뭄바이", "IN", "인도", "Asia", "🇮🇳", 19.0760, 72.8777);
        addCity(seeds, "모스크바", "moscow", "모스크바", "RU", "러시아", "Europe", "🇷🇺", 55.7558, 37.6173);
        addCity(seeds, "리우", "rio", "리우데자네이루", "BR", "브라질", "South America", "🇧🇷", -22.9068, -43.1729);
        // 다카 (방글라데시 수도)
        addCity(seeds, "다카", "dhaka", "다카", "BD", "방글라데시", "Asia", "🇧🇩", 23.8103, 90.4125);

        cityKeywordRepository.saveAll(seeds);
        logger.info("도시 키워드 {}개 시딩 완료", seeds.size());
    }

    private void addCountry(List<CountryKeyword> seeds, String keyword, String code, String name,
                             String continent, String emoji) {
        CountryKeyword entity = new CountryKeyword();
        entity.setKeyword(keyword.toLowerCase());
        entity.setCountryCode(code);
        entity.setCountryName(name);
        entity.setContinent(continent);
        entity.setCountryEmoji(emoji);
        seeds.add(entity);
    }

    private void addCity(List<CityKeyword> seeds, String koreanKeyword, String englishKeyword, String cityName,
                          String countryCode, String countryName, String continent, String countryEmoji,
                          double latitude, double longitude) {
        for (String keyword : new String[]{koreanKeyword, englishKeyword}) {
            CityKeyword entity = new CityKeyword();
            entity.setKeyword(keyword.toLowerCase());
            entity.setCityName(cityName);
            entity.setLatitude(latitude);
            entity.setLongitude(longitude);
            entity.setCountryCode(countryCode);
            entity.setCountryName(countryName);
            entity.setContinent(continent);
            entity.setCountryEmoji(countryEmoji);
            seeds.add(entity);
        }
    }
}
