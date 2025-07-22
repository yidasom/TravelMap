package com.travelmap.service;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.travelmap.entity.User;
import com.travelmap.entity.Video;
import com.travelmap.repository.UserRepository;
import com.travelmap.repository.VideoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class YouTubeService {
    
    private static final Logger logger = LoggerFactory.getLogger(YouTubeService.class);
    
    private final YouTube youtube;
    private final String apiKey;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    
    @Autowired
    public YouTubeService(YouTube youtube, 
                         @Value("${youtube.api.key}") String apiKey,
                         UserRepository userRepository,
                         VideoRepository videoRepository) {
        this.youtube = youtube;
        this.apiKey = apiKey;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
    }
    
    /**
     * 채널 정보를 가져와 User 엔티티로 저장
     */
    public User saveChannelInfo(String channelId) throws IOException {
        logger.info("채널 정보 수집 시작: {}", channelId);
        
        // 이미 존재하는 채널인지 확인
        Optional<User> existingUser = userRepository.findByYoutubeChannelId(channelId);
        if (existingUser.isPresent()) {
            logger.info("이미 존재하는 채널: {}", channelId);
            return existingUser.get();
        }
        
        // YouTube.Channels.List channelRequest = youtube.channels().list("snippet,statistics");
        // channelRequest.setId(channelId);
        // channelRequest.setKey(apiKey);
        
        // ChannelListResponse response = channelRequest.execute();
        // List<Channel> channels = response.getItems();
        
        // if (channels.isEmpty()) {
        //     throw new RuntimeException("채널을 찾을 수 없습니다: " + channelId);
        // }
        
        // Channel channel = channels.get(0);
        // ChannelSnippet snippet = channel.getSnippet();
        // ChannelStatistics statistics = channel.getStatistics();
        
        User user = new User();
        // user.setName(snippet.getTitle());
        // user.setYoutubeChannelId(channelId);
        // user.setChannelUrl("https://www.youtube.com/channel/" + channelId);
        // user.setDescription(snippet.getDescription());
        // user.setProfileImageUrl(snippet.getThumbnails().getDefault().getUrl());
        
        // if (statistics != null) {
        //     user.setSubscriberCount(statistics.getSubscriberCount() != null ? 
        //         statistics.getSubscriberCount().longValue() : 0L);
        //     user.setTotalViewCount(statistics.getViewCount() != null ? 
        //         statistics.getViewCount().longValue() : 0L);
        //     user.setTotalVideoCount(statistics.getVideoCount() != null ? 
        //         statistics.getVideoCount().longValue() : 0L);
        // }
        
        User savedUser = userRepository.save(user);
        logger.info("채널 정보 저장 완료: {} ({})", savedUser.getName(), savedUser.getId());
        
        return savedUser;
    }
    
    /**
     * 채널의 최신 영상들을 가져와서 저장
     */
    public List<Video> saveChannelVideos(String channelId, int maxResults) throws IOException {
        logger.info("채널 영상 수집 시작: {}, 최대 {}개", channelId, maxResults);
        
        User user = userRepository.findByYoutubeChannelId(channelId)
                .orElseThrow(() -> new RuntimeException("채널 정보를 먼저 저장해야 합니다: " + channelId));
        
        // 채널의 업로드 플레이리스트 ID 가져오기
        // YouTube.Channels.List channelRequest = youtube.channels().list("contentDetails");
        // channelRequest.setId(channelId);
        // channelRequest.setKey(apiKey);
        
        // ChannelListResponse channelResponse = channelRequest.execute();
        // if (channelResponse.getItems().isEmpty()) {
        //     throw new RuntimeException("채널을 찾을 수 없습니다: " + channelId);
        // }
        
        // String uploadsPlaylistId = channelResponse.getItems().get(0)
        //         .getContentDetails().getRelatedPlaylists().getUploads();
        
        // 플레이리스트에서 영상 목록 가져오기
        // YouTube.PlaylistItems.List playlistRequest = youtube.playlistItems().list("snippet");
        // playlistRequest.setPlaylistId(uploadsPlaylistId);
        // playlistRequest.setMaxResults((long) maxResults);
        // playlistRequest.setKey(apiKey);
        
        // PlaylistItemListResponse playlistResponse = playlistRequest.execute();
        // List<PlaylistItem> playlistItems = playlistResponse.getItems();
        
        List<Video> savedVideos = new ArrayList<>();
        
        // for (PlaylistItem item : playlistItems) {
        //     PlaylistItemSnippet snippet = item.getSnippet();
        //     String videoId = snippet.getResourceId().getVideoId();
            
        //     // 이미 존재하는 영상인지 확인
        //     if (videoRepository.existsByVideoId(videoId)) {
        //         logger.debug("이미 존재하는 영상 건너뛰기: {}", videoId);
        //         continue;
        //     }
            
        //     Video video = new Video();
        //     video.setVideoId(videoId);
        //     video.setTitle(snippet.getTitle());
        //     video.setDescription(snippet.getDescription());
        //     video.setThumbnailUrl(snippet.getThumbnails().getDefault().getUrl());
        //     video.setVideoUrl("https://www.youtube.com/watch?v=" + videoId);
        //     video.setUser(user);
            
        //     // 업로드 날짜 파싱
        //     if (snippet.getPublishedAt() != null) {
        //         String publishedAt = snippet.getPublishedAt().toString();
        //         video.setUploadDate(LocalDateTime.parse(publishedAt.substring(0, 19)));
        //     }
            
        //     Video savedVideo = videoRepository.save(video);
        //     savedVideos.add(savedVideo);
            
        //     logger.debug("영상 저장 완료: {} ({})", savedVideo.getTitle(), savedVideo.getId());
        // }
        
        logger.info("채널 영상 수집 완료: {}개 저장", savedVideos.size());
        return savedVideos;
    }
    
    /**
     * 특정 영상의 상세 정보를 가져와서 업데이트
     */
    public Video updateVideoDetails(String videoId) throws IOException {
        logger.info("영상 상세 정보 업데이트 시작: {}", videoId);
        
        Video video = videoRepository.findByVideoId(videoId)
                .orElseThrow(() -> new RuntimeException("영상을 찾을 수 없습니다: " + videoId));
        
        // YouTube.Videos.List videoRequest = youtube.videos().list("snippet,statistics,contentDetails");
        // videoRequest.setId(videoId);
        // videoRequest.setKey(apiKey);
        
        // VideoListResponse response = videoRequest.execute();
        // List<com.google.api.services.youtube.model.Video> videos = response.getItems();
        
        // if (videos.isEmpty()) {
        //     throw new RuntimeException("YouTube에서 영상을 찾을 수 없습니다: " + videoId);
        // }
        
        // com.google.api.services.youtube.model.Video youtubeVideo = videos.get(0);
        // VideoSnippet snippet = youtubeVideo.getSnippet();
        // VideoStatistics statistics = youtubeVideo.getStatistics();
        // VideoContentDetails contentDetails = youtubeVideo.getContentDetails();
        
        // // 상세 정보 업데이트
        // if (statistics != null) {
        //     video.setViewCount(statistics.getViewCount() != null ? 
        //         statistics.getViewCount().longValue() : 0L);
        //     video.setLikeCount(statistics.getLikeCount() != null ? 
        //         statistics.getLikeCount().longValue() : 0L);
        //     video.setCommentCount(statistics.getCommentCount() != null ? 
        //         statistics.getCommentCount().longValue() : 0L);
        // }
        
        // if (contentDetails != null) {
        //     video.setDuration(contentDetails.getDuration());
        // }
        
        Video savedVideo = videoRepository.save(video);
        // logger.info("영상 상세 정보 업데이트 완료: {}", savedVideo.getId());
        
        return savedVideo;
    }
    
    /**
     * 처리되지 않은 영상들의 상세 정보를 일괄 업데이트
     */
    public void updateUnprocessedVideosDetails() {
        logger.info("처리되지 않은 영상들의 상세 정보 업데이트 시작");
        
        List<Video> unprocessedVideos = videoRepository.findByProcessedFalse();
        logger.info("처리할 영상 수: {}", unprocessedVideos.size());
        
        for (Video video : unprocessedVideos) {
            try {
                updateVideoDetails(video.getVideoId());
                video.setProcessed(true);
                videoRepository.save(video);
            } catch (Exception e) {
                logger.error("영상 상세 정보 업데이트 실패: {} - {}", video.getVideoId(), e.getMessage());
            }
        }
        
        logger.info("처리되지 않은 영상들의 상세 정보 업데이트 완료");
    }
} 