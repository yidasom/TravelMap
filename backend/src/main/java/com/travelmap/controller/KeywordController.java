package com.travelmap.controller;

import com.travelmap.dto.CityKeywordDto;
import com.travelmap.dto.CountryKeywordDto;
import com.travelmap.entity.CityKeyword;
import com.travelmap.entity.CountryKeyword;
import com.travelmap.repository.CityKeywordRepository;
import com.travelmap.repository.CountryKeywordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 국가/도시 감지 키워드 관리 API.
 * CountryDetectionService가 영상 제목에서 국가·도시를 찾을 때 쓰는 키워드를 코드 배포 없이 추가/삭제할 수 있게 해준다.
 */
@RestController
@RequestMapping("/api/admin/keywords")
@CrossOrigin(origins = "*")
public class KeywordController {

    private static final Logger logger = LoggerFactory.getLogger(KeywordController.class);

    private final CountryKeywordRepository countryKeywordRepository;
    private final CityKeywordRepository cityKeywordRepository;

    @Autowired
    public KeywordController(CountryKeywordRepository countryKeywordRepository,
                              CityKeywordRepository cityKeywordRepository) {
        this.countryKeywordRepository = countryKeywordRepository;
        this.cityKeywordRepository = cityKeywordRepository;
    }

    // ---------- 국가 키워드 ----------

    @GetMapping("/countries")
    public ResponseEntity<List<CountryKeywordDto>> getCountryKeywords() {
        List<CountryKeywordDto> dtos = countryKeywordRepository.findAllByOrderByCountryNameAscKeywordAsc()
                .stream().map(CountryKeywordDto::new).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/countries")
    public ResponseEntity<?> addCountryKeyword(@RequestBody CountryKeywordDto request) {
        try {
            if (isBlank(request.getKeyword()) || isBlank(request.getCountryCode()) || isBlank(request.getCountryName())) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error", "message", "keyword, countryCode, countryName은 필수입니다."));
            }

            String normalizedKeyword = request.getKeyword().trim().toLowerCase();
            if (countryKeywordRepository.existsByKeywordIgnoreCase(normalizedKeyword)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error", "message", "이미 등록된 키워드입니다: " + normalizedKeyword));
            }

            CountryKeyword entity = new CountryKeyword();
            entity.setKeyword(normalizedKeyword);
            entity.setCountryCode(request.getCountryCode().trim().toUpperCase());
            entity.setCountryName(request.getCountryName().trim());
            entity.setContinent(request.getContinent());
            entity.setCountryEmoji(request.getCountryEmoji());

            entity = countryKeywordRepository.save(entity);
            logger.info("국가 키워드 추가: {} -> {} ({})", normalizedKeyword, entity.getCountryName(), entity.getCountryCode());

            return ResponseEntity.ok(new CountryKeywordDto(entity));
        } catch (Exception e) {
            logger.error("국가 키워드 추가 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error", "message", "국가 키워드 추가에 실패했습니다."));
        }
    }

    @DeleteMapping("/countries/{id}")
    public ResponseEntity<?> deleteCountryKeyword(@PathVariable Long id) {
        if (!countryKeywordRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        countryKeywordRepository.deleteById(id);
        logger.info("국가 키워드 삭제: id={}", id);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    // ---------- 도시 키워드 ----------

    @GetMapping("/cities")
    public ResponseEntity<List<CityKeywordDto>> getCityKeywords() {
        List<CityKeywordDto> dtos = cityKeywordRepository.findAllByOrderByCityNameAscKeywordAsc()
                .stream().map(CityKeywordDto::new).collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/cities")
    public ResponseEntity<?> addCityKeyword(@RequestBody CityKeywordDto request) {
        try {
            if (isBlank(request.getKeyword()) || isBlank(request.getCityName())
                    || isBlank(request.getCountryCode()) || isBlank(request.getCountryName())
                    || request.getLatitude() == null || request.getLongitude() == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "keyword, cityName, latitude, longitude, countryCode, countryName은 필수입니다."));
            }

            String normalizedKeyword = request.getKeyword().trim().toLowerCase();
            if (cityKeywordRepository.existsByKeywordIgnoreCase(normalizedKeyword)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error", "message", "이미 등록된 키워드입니다: " + normalizedKeyword));
            }

            CityKeyword entity = new CityKeyword();
            entity.setKeyword(normalizedKeyword);
            entity.setCityName(request.getCityName().trim());
            entity.setLatitude(request.getLatitude());
            entity.setLongitude(request.getLongitude());
            entity.setCountryCode(request.getCountryCode().trim().toUpperCase());
            entity.setCountryName(request.getCountryName().trim());
            entity.setContinent(request.getContinent());
            entity.setCountryEmoji(request.getCountryEmoji());

            entity = cityKeywordRepository.save(entity);
            logger.info("도시 키워드 추가: {} -> {} ({})", normalizedKeyword, entity.getCityName(), entity.getCountryCode());

            return ResponseEntity.ok(new CityKeywordDto(entity));
        } catch (Exception e) {
            logger.error("도시 키워드 추가 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "status", "error", "message", "도시 키워드 추가에 실패했습니다."));
        }
    }

    @DeleteMapping("/cities/{id}")
    public ResponseEntity<?> deleteCityKeyword(@PathVariable Long id) {
        if (!cityKeywordRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        cityKeywordRepository.deleteById(id);
        logger.info("도시 키워드 삭제: id={}", id);
        return ResponseEntity.ok(Map.of("status", "success"));
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
