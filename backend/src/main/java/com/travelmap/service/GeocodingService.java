package com.travelmap.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelmap.entity.CityKeyword;
import com.travelmap.entity.CountryKeyword;
import com.travelmap.repository.CityKeywordRepository;
import com.travelmap.repository.CountryKeywordRepository;
import com.travelmap.service.CountryDetectionService.CountryInfo;
import com.travelmap.service.CountryDetectionService.DetectedLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 키워드 사전(country_keywords/city_keywords)으로 못 찾은 지명을
 * OpenStreetMap Nominatim(무료 지오코딩 API, 키 불필요)으로 탐지하는 폴백 서비스.
 *
 * 성공적으로 찾은 지명은 키워드 테이블에 자동 등록(자가 학습)되어,
 * 같은 지명은 다음부터 외부 API 호출 없이 키워드 매칭으로 바로 잡힌다.
 * 관리자 키워드 화면은 오탐 수정/예외 등록용으로만 쓰면 된다.
 *
 * Nominatim 사용 정책: 초당 1회 요청 제한, 식별 가능한 User-Agent 필수.
 */
@Service
public class GeocodingService {

    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";
    private static final String USER_AGENT = "TravelMap/0.1 (personal project; github.com/yidasom/TravelMap)";

    // 국가/도시급 행정구역으로 인정할 addresstype (산·강·식당 같은 잡음 배제)
    private static final Set<String> PLACE_ADDRESS_TYPES = Set.of(
            "country", "state", "province", "region", "county", "city", "town",
            "village", "municipality", "island", "territory");

    // 이 값 미만의 importance는 무명 지명 오탐으로 보고 버린다
    private static final double MIN_IMPORTANCE = 0.3;

    // 제목 하나당 Nominatim 조회 상한 (초당 1회 제한이라 과도한 지연 방지)
    private static final int MAX_QUERIES_PER_TITLE = 8;

    // 한국어 조사(뒤에 붙는 것만): "치앙마이에" -> "치앙마이" 변형도 후보로 추가
    private static final String[] PARTICLES = {
            "에서는", "에서도", "에서", "으로", "까지", "부터", "에는", "에도",
            "은", "는", "이", "가", "을", "를", "의", "와", "과", "도", "로", "만", "에"
    };

    // 국가 코드 -> 대륙 매핑 (지오코딩 결과에는 대륙 정보가 없어서 자체 보완)
    private static final Map<String, String> CONTINENT_BY_CODE = new HashMap<>();

    static {
        mapContinent("Asia", "KR,JP,CN,TW,HK,MO,MN,TH,VN,SG,MY,ID,PH,IN,BD,LK,NP,KH,LA,MM,BN,TL,PK,AF,IR,IQ,SA,AE,QA,KW,BH,OM,YE,JO,IL,PS,LB,SY,TR,GE,AM,AZ,KZ,KG,TJ,TM,UZ,MV,BT,CY");
        mapContinent("Europe", "GB,FR,DE,IT,ES,PT,NL,BE,LU,CH,AT,IE,IS,NO,SE,FI,DK,PL,CZ,SK,HU,RO,BG,GR,HR,SI,RS,BA,ME,MK,AL,XK,EE,LV,LT,BY,UA,MD,RU,MT,MC,AD,SM,VA,LI");
        mapContinent("North America", "US,CA,MX,GT,BZ,SV,HN,NI,CR,PA,CU,JM,HT,DO,BS,TT,PR");
        mapContinent("South America", "BR,AR,CL,PE,CO,VE,EC,BO,PY,UY,GY,SR");
        mapContinent("Africa", "EG,MA,DZ,TN,LY,SD,SS,ET,KE,TZ,UG,RW,BI,CD,CG,GA,CM,NG,GH,CI,SN,ML,BF,NE,TD,MR,ZA,ZW,ZM,MW,MZ,BW,NA,AO,MG,SO,DJ,ER,GM,GN,GW,SL,LR,TG,BJ,CF,GQ,ST,CV,KM,MU,SC,LS,SZ");
        mapContinent("Oceania", "AU,NZ,FJ,PG,SB,VU,NC,PF,WS,TO,KI,FM,MH,PW,NR,TV");
    }

    private static void mapContinent(String continent, String csvCodes) {
        for (String code : csvCodes.split(",")) {
            CONTINENT_BY_CODE.put(code, continent);
        }
    }

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final CountryKeywordRepository countryKeywordRepository;
    private final CityKeywordRepository cityKeywordRepository;
    private final boolean enabled;

    // 지명이 아닌 걸로 판명된 단어 캐시 (재기동 시 초기화되지만 배치 중 반복 조회를 크게 줄여준다)
    private final Set<String> negativeCache = ConcurrentHashMap.newKeySet();

    // Nominatim 초당 1회 제한 준수용
    private long lastRequestAt = 0;

    @Autowired
    public GeocodingService(CountryKeywordRepository countryKeywordRepository,
                             CityKeywordRepository cityKeywordRepository,
                             ObjectMapper objectMapper,
                             @Value("${geocoding.enabled:true}") boolean enabled) {
        this.countryKeywordRepository = countryKeywordRepository;
        this.cityKeywordRepository = cityKeywordRepository;
        this.objectMapper = objectMapper;
        this.enabled = enabled;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(5000);
        this.restTemplate = new RestTemplate(factory);
    }

    /**
     * 제목을 단어 단위로 쪼개 Nominatim에 조회해서 국가/도시를 찾는다.
     * 첫 번째로 확실하게 매칭된 지명 하나만 반환한다 (오탐 누적 및 API 부하 방지).
     */
    public List<DetectedLocation> detectLocationsFromTitle(String title) {
        List<DetectedLocation> result = new ArrayList<>();

        if (!enabled || title == null || title.trim().isEmpty()) {
            return result;
        }

        int queries = 0;
        for (String token : extractCandidateTokens(title)) {
            if (queries >= MAX_QUERIES_PER_TITLE) {
                break;
            }

            String cacheKey = token.toLowerCase();
            if (negativeCache.contains(cacheKey)) {
                continue;
            }

            queries++;
            try {
                DetectedLocation location = geocodeToken(token);
                if (location != null) {
                    logger.info("지오코딩으로 지명 탐지: '{}' -> {} {}", token,
                            location.getCountry().getName(),
                            location.getCityName() != null ? "/ " + location.getCityName() : "");
                    result.add(location);
                    learnKeyword(token, location);
                    break;
                }
                negativeCache.add(cacheKey);
            } catch (Exception e) {
                // 네트워크 오류 등 일시 장애는 캐시하지 않는다 (다음에 다시 시도할 수 있게)
                logger.warn("지오코딩 요청 실패, 건너뜀: '{}' - {}", token, e.getMessage());
            }
        }

        return result;
    }

    /**
     * 단일 단어를 Nominatim으로 조회. 국가/도시급 지명으로 확인되면 DetectedLocation, 아니면 null.
     */
    private DetectedLocation geocodeToken(String token) throws Exception {
        JsonNode item = queryNominatim(token);
        if (item == null) {
            return null;
        }

        String category = item.path("category").asText("");
        String addressType = item.path("addresstype").asText("");
        double importance = item.path("importance").asDouble(0.0);

        if (!("place".equals(category) || "boundary".equals(category))
                || !PLACE_ADDRESS_TYPES.contains(addressType)
                || importance < MIN_IMPORTANCE) {
            return null;
        }

        JsonNode address = item.path("address");
        String countryCode = address.path("country_code").asText("").toUpperCase();
        String countryName = address.path("country").asText("");
        if (countryCode.isEmpty() || countryName.isEmpty()) {
            return null;
        }

        CountryInfo country = new CountryInfo(
                countryCode, countryName,
                CONTINENT_BY_CODE.get(countryCode),
                flagEmojiFromCode(countryCode));

        if ("country".equals(addressType)) {
            return new DetectedLocation(country, null, null, null, "GEOCODED");
        }

        String cityName = item.path("name").asText("");
        if (cityName.isEmpty()) {
            cityName = token;
        }
        double lat = item.path("lat").asDouble();
        double lon = item.path("lon").asDouble();

        return new DetectedLocation(country, cityName, lat, lon, "GEOCODED");
    }

    /**
     * Nominatim 호출 (초당 1회 제한 준수). 결과가 없으면 null.
     */
    private synchronized JsonNode queryNominatim(String term) throws Exception {
        long sinceLast = System.currentTimeMillis() - lastRequestAt;
        if (sinceLast < 1100) {
            Thread.sleep(1100 - sinceLast);
        }
        lastRequestAt = System.currentTimeMillis();

        String url = UriComponentsBuilder.fromHttpUrl(NOMINATIM_URL)
                .queryParam("q", term)
                .queryParam("format", "jsonv2")
                .queryParam("limit", "1")
                .queryParam("accept-language", "ko")
                .queryParam("addressdetails", "1")
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT);

        ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        JsonNode root = objectMapper.readTree(response.getBody());
        if (!root.isArray() || root.isEmpty()) {
            return null;
        }
        return root.get(0);
    }

    /**
     * 지오코딩으로 찾은 지명을 키워드 테이블에 자동 등록해서, 다음부터는 API 호출 없이 매칭되게 한다.
     */
    private void learnKeyword(String token, DetectedLocation location) {
        try {
            String keyword = token.trim().toLowerCase();
            if (keyword.length() < 2) {
                return;
            }

            CountryInfo country = location.getCountry();

            if (location.getCityName() == null) {
                if (!countryKeywordRepository.existsByKeywordIgnoreCase(keyword)) {
                    CountryKeyword entity = new CountryKeyword();
                    entity.setKeyword(keyword);
                    entity.setCountryCode(country.getCode());
                    entity.setCountryName(country.getName());
                    entity.setContinent(country.getContinent());
                    entity.setCountryEmoji(country.getEmoji());
                    countryKeywordRepository.save(entity);
                    logger.info("국가 키워드 자동 학습: '{}' -> {} ({})", keyword, country.getName(), country.getCode());
                }
            } else {
                if (!cityKeywordRepository.existsByKeywordIgnoreCase(keyword)) {
                    CityKeyword entity = new CityKeyword();
                    entity.setKeyword(keyword);
                    entity.setCityName(location.getCityName());
                    entity.setLatitude(location.getCityLatitude());
                    entity.setLongitude(location.getCityLongitude());
                    entity.setCountryCode(country.getCode());
                    entity.setCountryName(country.getName());
                    entity.setContinent(country.getContinent());
                    entity.setCountryEmoji(country.getEmoji());
                    cityKeywordRepository.save(entity);
                    logger.info("도시 키워드 자동 학습: '{}' -> {} ({})", keyword, location.getCityName(), country.getName());
                }
            }
        } catch (Exception e) {
            // 학습 실패해도 이번 감지 결과 자체는 유효하므로 무시하고 진행
            logger.warn("키워드 자동 학습 실패: '{}' - {}", token, e.getMessage());
        }
    }

    /**
     * 제목에서 지명 후보 단어를 추출한다. 원형과 조사 제거형을 모두 후보에 넣는다.
     */
    private List<String> extractCandidateTokens(String title) {
        LinkedHashSet<String> tokens = new LinkedHashSet<>();

        String cleaned = title.replaceAll("[^\\p{L}\\p{Nd}]+", " ").trim();
        if (cleaned.isEmpty()) {
            return new ArrayList<>();
        }

        for (String raw : cleaned.split("\\s+")) {
            if (raw.length() < 2 || raw.length() > 20 || raw.matches("\\p{Nd}+")) {
                continue;
            }
            tokens.add(raw);

            for (String particle : PARTICLES) {
                if (raw.endsWith(particle) && raw.length() - particle.length() >= 2) {
                    tokens.add(raw.substring(0, raw.length() - particle.length()));
                    break;
                }
            }
        }

        return new ArrayList<>(tokens);
    }

    /**
     * ISO 국가 코드 2자리 -> 국기 이모지 (유니코드 지역 표시 문자 조합)
     */
    private String flagEmojiFromCode(String countryCode) {
        StringBuilder sb = new StringBuilder();
        for (char c : countryCode.toCharArray()) {
            sb.appendCodePoint(0x1F1E6 + (c - 'A'));
        }
        return sb.toString();
    }
}
