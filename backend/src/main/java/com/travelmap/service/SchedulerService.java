package com.travelmap.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    
    @Autowired
    private DataCollectionService dataCollectionService;
    
    /**
     * 매일 새벽 2시에 데이터 수집 실행
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void scheduledDataCollection() {
        logger.info("스케줄링된 데이터 수집 시작");
        
        try {
            dataCollectionService.collectAllData();
            logger.info("스케줄링된 데이터 수집 완료");
        } catch (Exception e) {
            logger.error("스케줄링된 데이터 수집 실패", e);
        }
    }
    
    /**
     * 매주 일요일 오후 1시에 전체 데이터 업데이트
     */
    @Scheduled(cron = "0 0 13 * * SUN")
    public void weeklyDataUpdate() {
        logger.info("주간 데이터 업데이트 시작");
        
        try {
            dataCollectionService.updateAllChannelsData();
            logger.info("주간 데이터 업데이트 완료");
        } catch (Exception e) {
            logger.error("주간 데이터 업데이트 실패", e);
        }
    }
    
    /**
     * 매시간 처리되지 않은 영상들의 상세 정보 업데이트
     */
    @Scheduled(fixedRate = 3600000) // 1시간마다
    public void hourlyVideoProcessing() {
        logger.info("시간별 영상 처리 시작");
        
        try {
            dataCollectionService.processUnprocessedVideos();
            logger.info("시간별 영상 처리 완료");
        } catch (Exception e) {
            logger.error("시간별 영상 처리 실패", e);
        }
    }
} 