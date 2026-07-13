package com.travelmap.controller;

import com.travelmap.service.DataCollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 데이터 수집 관리(어드민) 엔드포인트.
 * 지도/영상 조회는 {@link TravelMapController}에서 담당하고, 여긴 유튜브 채널 데이터 수집을 트리거하는 쪽만 모아둔다.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private final DataCollectionService dataCollectionService;

    @Autowired
    public AdminController(DataCollectionService dataCollectionService) {
        this.dataCollectionService = dataCollectionService;
    }

    /**
     * 전체 데이터 수집 시작
     */
    @PostMapping("/collect-all")
    public ResponseEntity<Map<String, Object>> collectAllData() {
        logger.info("전체 데이터 수집 요청");

        try {
            Map<String, Object> result = dataCollectionService.collectAllData();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("전체 데이터 수집 API 오류", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 특정 채널 데이터 수집
     */
    @PostMapping("/collect-channel")
    public ResponseEntity<Map<String, Object>> collectChannelData(@RequestParam String searchQuery) {
        logger.info("특정 채널 데이터 수집 요청: {}", searchQuery);

        try {
            Map<String, Object> result = dataCollectionService.collectChannelData(searchQuery);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("특정 채널 데이터 수집 API 오류", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 전체 채널 데이터 업데이트
     */
    @PostMapping("/update-all")
    public ResponseEntity<Map<String, Object>> updateAllChannelsData() {
        logger.info("전체 채널 데이터 업데이트 요청");

        try {
            Map<String, Object> result = dataCollectionService.updateAllChannelsData();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("전체 채널 데이터 업데이트 API 오류", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 처리되지 않은 영상들 처리
     */
    @PostMapping("/process-unprocessed")
    public ResponseEntity<Map<String, Object>> processUnprocessedVideos() {
        logger.info("처리되지 않은 영상들 처리 요청");

        try {
            Map<String, Object> result = dataCollectionService.processUnprocessedVideos();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("처리되지 않은 영상들 처리 API 오류", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 수집 상태 조회
     */
    @GetMapping("/collection-status")
    public ResponseEntity<Map<String, Object>> getCollectionStatus() {
        try {
            Map<String, Object> status = dataCollectionService.getCollectionStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("수집 상태 조회 API 오류", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "서버 오류가 발생했습니다."));
        }
    }

    /**
     * 새 채널 추가
     */
    @PostMapping("/add-channel")
    public ResponseEntity<Map<String, Object>> addNewChannel(
            @RequestParam String searchQuery,
            @RequestParam(required = false) String channelName) {
        logger.info("새 채널 추가 요청: {} ({})", channelName, searchQuery);

        try {
            Map<String, Object> result = dataCollectionService.addNewChannel(searchQuery, channelName);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("새 채널 추가 API 오류", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("status", "error", "message", "서버 오류가 발생했습니다."));
        }
    }
}
