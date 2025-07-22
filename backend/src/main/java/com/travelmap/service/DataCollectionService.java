package com.travelmap.service;

import com.travelmap.entity.User;
import com.travelmap.entity.Video;
import com.travelmap.repository.UserRepository;
import com.travelmap.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Transactional
public class DataCollectionService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataCollectionService.class);
    
    @Autowired
    private YouTubeService youTubeService;
    
    @Autowired
    private CountryDetectionService countryDetectionService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private VideoRepository videoRepository;
    
    private volatile boolean isCollecting = false;
    private volatile String currentStatus = "대기 중";
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private final AtomicInteger totalCount = new AtomicInteger(0);
    
    /**
     * 모든 채널의 데이터 수집
     */
    public Map<String, Object> collectAllData() {
        if (isCollecting) {
            return Map.of(
                "status", "error",
                "message", "이미 수집 작업이 진행 중입니다."
            );
        }
        
        logger.info("전체 데이터 수집 시작");
        isCollecting = true;
        currentStatus = "전체 데이터 수집 중...";
        processedCount.set(0);
        
        try {
            List<User> users = userRepository.findAll();
            totalCount.set(users.size());
            
            for (User user : users) {
                try {
                    currentStatus = String.format("채널 처리 중: %s", user.getName());
                    collectChannelData(user.getSearchQuery() != null ? user.getSearchQuery() : user.getName());
                    processedCount.incrementAndGet();
                } catch (Exception e) {
                    logger.error("채널 데이터 수집 실패: {}", user.getName(), e);
                }
            }
            
            currentStatus = "완료";
            logger.info("전체 데이터 수집 완료");
            
            return Map.of(
                "status", "success",
                "message", "전체 데이터 수집이 완료되었습니다.",
                "processedCount", processedCount.get(),
                "totalCount", totalCount.get()
            );
            
        } catch (Exception e) {
            logger.error("전체 데이터 수집 실패", e);
            currentStatus = "오류 발생";
            
            return Map.of(
                "status", "error",
                "message", "데이터 수집 중 오류가 발생했습니다: " + e.getMessage()
            );
        } finally {
            isCollecting = false;
        }
    }
    
    /**
     * 특정 채널의 데이터 수집
     */
    public Map<String, Object> collectChannelData(String searchQuery) {
        logger.info("채널 데이터 수집 시작: {}", searchQuery);
        
        try {
            // 1. 채널 정보 저장
            User user = youTubeService.saveChannelInfo(searchQuery);
            
            // 2. 최신 영상 50개 수집
            List<Video> videos = youTubeService.saveChannelVideos(user.getYoutubeChannelId(), 50);
            
            // 3. 비동기로 국가 감지 처리
            CompletableFuture.runAsync(() -> {
                for (Video video : videos) {
                    try {
                        countryDetectionService.extractCountriesFromTitle(video);
                    } catch (Exception e) {
                        logger.error("국가 감지 실패: {}", video.getVideoId(), e);
                    }
                }
            });
            
            return Map.of(
                "status", "success",
                "message", String.format("채널 데이터 수집 완료: %d개 영상", videos.size()),
                "channelName", user.getName(),
                "videoCount", videos.size()
            );
            
        } catch (Exception e) {
            logger.error("채널 데이터 수집 실패: {}", searchQuery, e);
            return Map.of(
                "status", "error",
                "message", "채널 데이터 수집 실패: " + e.getMessage()
            );
        }
    }
    
    /**
     * 모든 채널의 데이터 업데이트 (기존 데이터 갱신)
     */
    public Map<String, Object> updateAllChannelsData() {
        logger.info("전체 채널 데이터 업데이트 시작");
        
        if (isCollecting) {
            return Map.of(
                "status", "error",
                "message", "이미 수집 작업이 진행 중입니다."
            );
        }
        
        isCollecting = true;
        currentStatus = "데이터 업데이트 중...";
        processedCount.set(0);
        
        try {
            List<User> users = userRepository.findAll();
            totalCount.set(users.size());
            
            for (User user : users) {
                try {
                    currentStatus = String.format("채널 업데이트 중: %s", user.getName());
                    
                    // 채널 정보 재수집
                    youTubeService.saveChannelInfo(user.getYoutubeChannelId());
                    
                    // 최신 영상 추가 수집
                    youTubeService.saveChannelVideos(user.getYoutubeChannelId(), 20);
                    
                    processedCount.incrementAndGet();
                } catch (Exception e) {
                    logger.error("채널 데이터 업데이트 실패: {}", user.getName(), e);
                }
            }
            
            currentStatus = "완료";
            
            return Map.of(
                "status", "success",
                "message", "전체 채널 데이터 업데이트가 완료되었습니다.",
                "updatedChannels", processedCount.get()
            );
            
        } catch (Exception e) {
            logger.error("전체 채널 데이터 업데이트 실패", e);
            currentStatus = "오류 발생";
            
            return Map.of(
                "status", "error",
                "message", "데이터 업데이트 중 오류가 발생했습니다: " + e.getMessage()
            );
        } finally {
            isCollecting = false;
        }
    }
    
    /**
     * 처리되지 않은 영상들 처리
     */
    public Map<String, Object> processUnprocessedVideos() {
        logger.info("처리되지 않은 영상들 처리 시작");
        
        try {
            List<Video> unprocessedVideos = videoRepository.findByProcessedFalse();
            
            if (unprocessedVideos.isEmpty()) {
                return Map.of(
                    "status", "success",
                    "message", "처리할 영상이 없습니다."
                );
            }
            
            AtomicInteger processed = new AtomicInteger(0);
            
            for (Video video : unprocessedVideos) {
                try {
                    // 영상 상세 정보 업데이트
                    youTubeService.updateVideoDetails(video.getVideoId());
                    
                                         // 국가 감지
                     countryDetectionService.extractCountriesFromTitle(video);
                    
                    video.setProcessed(true);
                    videoRepository.save(video);
                    
                    processed.incrementAndGet();
                } catch (Exception e) {
                    logger.error("영상 처리 실패: {}", video.getVideoId(), e);
                }
            }
            
            return Map.of(
                "status", "success",
                "message", String.format("%d개 영상 처리 완료", processed.get()),
                "processedCount", processed.get(),
                "totalCount", unprocessedVideos.size()
            );
            
        } catch (Exception e) {
            logger.error("처리되지 않은 영상들 처리 실패", e);
            return Map.of(
                "status", "error",
                "message", "영상 처리 중 오류가 발생했습니다: " + e.getMessage()
            );
        }
    }
    
    /**
     * 현재 수집 상태 조회
     */
    public Map<String, Object> getCollectionStatus() {
        return Map.of(
            "isCollecting", isCollecting,
            "currentStatus", currentStatus,
            "processedCount", processedCount.get(),
            "totalCount", totalCount.get(),
            "progressPercentage", totalCount.get() > 0 ? 
                (processedCount.get() * 100.0 / totalCount.get()) : 0.0
        );
    }
    
    /**
     * 새 채널 추가
     */
    public Map<String, Object> addNewChannel(String searchQuery, String channelName) {
        logger.info("새 채널 추가: {} ({})", channelName, searchQuery);
        
        try {
            // 채널 정보 수집 및 저장
            User user = youTubeService.saveChannelInfo(searchQuery);
            
            // 이미 존재하는지 확인 (실제 채널 ID로)
            Optional<User> existingUser = userRepository.findByYoutubeChannelId(user.getYoutubeChannelId());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
                return Map.of(
                    "status", "error",
                    "message", "이미 등록된 채널입니다."
                );
            }
            
            // 최신 영상 수집
            List<Video> videos = youTubeService.saveChannelVideos(user.getYoutubeChannelId(), 50);
            
            // 비동기로 국가 감지
            CompletableFuture.runAsync(() -> {
                for (Video video : videos) {
                    try {
                        countryDetectionService.extractCountriesFromTitle(video);
                    } catch (Exception e) {
                        logger.error("국가 감지 실패: {}", video.getVideoId(), e);
                    }
                }
            });
            
            return Map.of(
                "status", "success",
                "message", String.format("새 채널이 추가되었습니다: %s", user.getName()),
                "channelName", user.getName(),
                "videoCount", videos.size()
            );
            
        } catch (Exception e) {
            logger.error("새 채널 추가 실패: {}", searchQuery, e);
            return Map.of(
                "status", "error",
                "message", "채널 추가 실패: " + e.getMessage()
            );
        }
    }
} 