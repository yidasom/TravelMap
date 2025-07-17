package com.travelmap.backend.controller;

import com.travelmap.backend.dto.*;
import com.travelmap.backend.entity.User;
import com.travelmap.backend.entity.Video;
import com.travelmap.backend.entity.VisitCountry;
import com.travelmap.backend.repository.UserRepository;
import com.travelmap.backend.repository.VideoRepository;
import com.travelmap.backend.repository.VisitCountryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TravelMapController {
    
    private static final Logger logger = LoggerFactory.getLogger(TravelMapController.class);
    
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final VisitCountryRepository visitCountryRepository;
    
    @Autowired
    public TravelMapController(UserRepository userRepository,
                              VideoRepository videoRepository,
                              VisitCountryRepository visitCountryRepository) {
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.visitCountryRepository = visitCountryRepository;
    }
    
    /**
     * 필터 옵션 목록 제공
     */
    @GetMapping("/filters")
    public ResponseEntity<FilterOptionsDto> getFilterOptions() {
        logger.info("필터 옵션 요청");
        
        try {
            // 유튜버 목록
            List<User> users = userRepository.findAll();
            List<UserDto> userDtos = users.stream()
                    .map(UserDto::new)
                    .collect(Collectors.toList());
            
            // 국가 목록
            List<String> countries = visitCountryRepository.findDistinctCountryNames();
            
            // 성별 목록
            List<String> genders = userRepository.findDistinctGenders();
            
            // 대륙 목록
            List<String> continents = visitCountryRepository.findDistinctContinents();
            
            // 연도 목록 (영상 업로드 연도 기준)
            List<String> years = getDistinctYears();
            
            FilterOptionsDto filterOptions = new FilterOptionsDto(
                    userDtos, countries, genders, years, continents
            );
            
            logger.info("필터 옵션 반환 완료");
            return ResponseEntity.ok(filterOptions);
            
        } catch (Exception e) {
            logger.error("필터 옵션 조회 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 지도 시각화 데이터 제공
     */
    @GetMapping("/map-data")
    public ResponseEntity<MapDataDto> getMapData(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        logger.info("지도 데이터 요청: userId={}, countryCode={}, gender={}, startDate={}, endDate={}", 
                   userId, countryCode, gender, startDate, endDate);
        
        try {
            // 필터링된 방문 국가 데이터 조회
            List<VisitCountry> visitCountries = visitCountryRepository.findByFilters(userId, countryCode, gender);
            
            // 국가별 집계
            Map<String, List<VisitCountry>> countryGroups = visitCountries.stream()
                    .collect(Collectors.groupingBy(VisitCountry::getCountryCode));
            
            List<MapDataDto.CountryDataDto> countryDataList = new ArrayList<>();
            
            for (Map.Entry<String, List<VisitCountry>> entry : countryGroups.entrySet()) {
                List<VisitCountry> countryVisits = entry.getValue();
                VisitCountry representative = countryVisits.get(0);
                
                Long visitCount = (long) countryVisits.size();
                Long youtuberCount = countryVisits.stream()
                        .map(vc -> vc.getVideo().getUser())
                        .distinct()
                        .count();
                
                // 해당 국가를 방문한 유튜버들
                List<UserDto> youtubers = countryVisits.stream()
                        .map(vc -> vc.getVideo().getUser())
                        .distinct()
                        .map(UserDto::new)
                        .collect(Collectors.toList());
                
                MapDataDto.CountryDataDto countryData = new MapDataDto.CountryDataDto(
                        representative.getCountryCode(),
                        representative.getCountryName(),
                        representative.getCountryEmoji(),
                        representative.getContinent(),
                        visitCount,
                        youtuberCount
                );
                countryData.setYoutubers(youtubers);
                
                countryDataList.add(countryData);
            }
            
            // 방문 횟수 기준 정렬
            countryDataList.sort((a, b) -> b.getVisitCount().compareTo(a.getVisitCount()));
            
            MapDataDto mapData = new MapDataDto(countryDataList);
            
            logger.info("지도 데이터 반환 완료: {}개 국가", countryDataList.size());
            return ResponseEntity.ok(mapData);
            
        } catch (Exception e) {
            logger.error("지도 데이터 조회 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 영상 목록 조회
     */
    @GetMapping("/videos")
    public ResponseEntity<List<VideoDto>> getVideos(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        logger.info("영상 목록 요청: userId={}, countryCode={}, gender={}, page={}, size={}", 
                   userId, countryCode, gender, page, size);
        
        try {
            List<Video> videos = videoRepository.findByFilters(userId, countryCode, gender, startDate, endDate);
            
            // 업로드 날짜 기준 내림차순 정렬
            videos.sort((a, b) -> {
                LocalDateTime dateA = a.getUploadDate() != null ? a.getUploadDate() : LocalDateTime.MIN;
                LocalDateTime dateB = b.getUploadDate() != null ? b.getUploadDate() : LocalDateTime.MIN;
                return dateB.compareTo(dateA);
            });
            
            // 페이징 처리
            int start = page * size;
            int end = Math.min(start + size, videos.size());
            List<Video> pagedVideos = videos.subList(start, end);
            
            List<VideoDto> videoDtos = pagedVideos.stream()
                    .map(VideoDto::new)
                    .collect(Collectors.toList());
            
            logger.info("영상 목록 반환 완료: {}개 (전체 {}개)", pagedVideos.size(), videos.size());
            return ResponseEntity.ok(videoDtos);
            
        } catch (Exception e) {
            logger.error("영상 목록 조회 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 개별 영상 상세 조회
     */
    @GetMapping("/videos/{id}")
    public ResponseEntity<VideoDto> getVideo(@PathVariable Long id) {
        logger.info("영상 상세 조회: {}", id);
        
        try {
            Optional<Video> videoOpt = videoRepository.findById(id);
            
            if (videoOpt.isEmpty()) {
                logger.warn("영상을 찾을 수 없음: {}", id);
                return ResponseEntity.notFound().build();
            }
            
            VideoDto videoDto = new VideoDto(videoOpt.get());
            
            logger.info("영상 상세 조회 완료: {}", videoDto.getTitle());
            return ResponseEntity.ok(videoDto);
            
        } catch (Exception e) {
            logger.error("영상 상세 조회 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 특정 국가의 영상 목록 조회
     */
    @GetMapping("/countries/{countryCode}/videos")
    public ResponseEntity<List<VideoDto>> getVideosByCountry(@PathVariable String countryCode) {
        logger.info("국가별 영상 조회: {}", countryCode);
        
        try {
            List<Video> videos = videoRepository.findByCountryCode(countryCode);
            
            List<VideoDto> videoDtos = videos.stream()
                    .map(VideoDto::new)
                    .collect(Collectors.toList());
            
            logger.info("국가별 영상 조회 완료: {}개", videoDtos.size());
            return ResponseEntity.ok(videoDtos);
            
        } catch (Exception e) {
            logger.error("국가별 영상 조회 오류", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 연도 목록 추출
     */
    private List<String> getDistinctYears() {
        List<Video> videos = videoRepository.findAll();
        return videos.stream()
                .filter(v -> v.getUploadDate() != null)
                .map(v -> String.valueOf(v.getUploadDate().getYear()))
                .distinct()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
    }
} 