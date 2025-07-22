import React from 'react';
import {
  Box,
  Card,
  CardContent,
  CardMedia,
  Typography,
  Grid,
  Chip,
  Avatar,
  CircularProgress,
  Alert,
  Button,
} from '@mui/material';
import {
  PlayArrow as PlayIcon,
  Visibility as ViewIcon,
  ThumbUp as LikeIcon,
  AccessTime as TimeIcon,
} from '@mui/icons-material';
import { Video } from '../types';

interface VideoListProps {
  videos: Video[];
  onVideoClick: (video: Video) => void;
  loading?: boolean;
  error?: string | null;
  onLoadMore?: () => void;
  hasMore?: boolean;
}

const VideoList: React.FC<VideoListProps> = ({
  videos,
  onVideoClick,
  loading = false,
  error = null,
  onLoadMore,
  hasMore = false,
}) => {
  const formatNumber = (num?: number): string => {
    if (!num) return '0';
    if (num >= 1000000) return `${(num / 1000000).toFixed(1)}M`;
    if (num >= 1000) return `${(num / 1000).toFixed(1)}K`;
    return num.toString();
  };

  const formatDate = (dateString?: string): string => {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const formatDuration = (duration?: string): string => {
    if (!duration) return '';
    // ISO 8601 duration format (PT4M13S) 파싱
    const match = duration.match(/PT(?:(\d+)H)?(?:(\d+)M)?(?:(\d+)S)?/);
    if (!match) return duration;
    
    const hours = parseInt(match[1] || '0');
    const minutes = parseInt(match[2] || '0');
    const seconds = parseInt(match[3] || '0');
    
    if (hours > 0) {
      return `${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }
    return `${minutes}:${seconds.toString().padStart(2, '0')}`;
  };

  if (error) {
    return (
      <Alert severity="error" sx={{ mt: 2 }}>
        {error}
      </Alert>
    );
  }

  if (loading && videos.length === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  if (videos.length === 0) {
    return (
      <Box sx={{ textAlign: 'center', mt: 4 }}>
        <Typography variant="h6" color="text.secondary">
          검색 결과가 없습니다
        </Typography>
        <Typography variant="body2" color="text.secondary">
          다른 필터 조건을 시도해보세요
        </Typography>
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="h6" sx={{ mb: 2 }}>
        영상 목록 ({videos.length}개)
      </Typography>
      
      <Grid container spacing={2}>
        {videos.map((video) => (
          <Grid item xs={12} sm={6} md={4} lg={3} key={video.id}>
            <Card
              sx={{
                cursor: 'pointer',
                transition: 'transform 0.2s, box-shadow 0.2s',
                '&:hover': {
                  transform: 'translateY(-4px)',
                  boxShadow: 4,
                },
              }}
              onClick={() => onVideoClick(video)}
            >
              {/* 썸네일 */}
              <Box sx={{ position: 'relative' }}>
                <CardMedia
                  component="img"
                  height="140"
                  image={video.thumbnailUrl || '/placeholder-thumbnail.png'}
                  alt={video.title}
                  sx={{ objectFit: 'cover' }}
                />
                {/* 재생 버튼 오버레이 */}
                <Box
                  sx={{
                    position: 'absolute',
                    top: 0,
                    left: 0,
                    right: 0,
                    bottom: 0,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    backgroundColor: 'rgba(0, 0, 0, 0.3)',
                    opacity: 0,
                    transition: 'opacity 0.2s',
                    '&:hover': { opacity: 1 },
                  }}
                >
                  <PlayIcon sx={{ fontSize: 40, color: 'white' }} />
                </Box>
                {/* 재생시간 */}
                {video.duration && (
                  <Chip
                    label={formatDuration(video.duration)}
                    size="small"
                    sx={{
                      position: 'absolute',
                      bottom: 8,
                      right: 8,
                      backgroundColor: 'rgba(0, 0, 0, 0.7)',
                      color: 'white',
                      fontWeight: 'bold',
                    }}
                  />
                )}
              </Box>

              <CardContent sx={{ pb: 1 }}>
                {/* 제목 */}
                <Typography
                  variant="subtitle2"
                  sx={{
                    fontWeight: 'bold',
                    lineHeight: 1.3,
                    height: '2.6em',
                    overflow: 'hidden',
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical',
                    mb: 1,
                  }}
                  title={video.title}
                >
                  {video.title}
                </Typography>

                {/* 유튜버 정보 */}
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 1 }}>
                  <Avatar
                    src={video.user.profileImageUrl}
                    alt={video.user.name}
                    sx={{ width: 24, height: 24 }}
                  />
                  <Typography variant="caption" color="text.secondary">
                    {video.user.name}
                  </Typography>
                  {/* {video.user.gender && (
                    <Chip label={video.user.gender} size="small" variant="outlined" />
                  )} */}
                </Box>

                {/* 통계 정보 */}
                <Box sx={{ display: 'flex', gap: 2, mb: 1 }}>
                  {video.viewCount && (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <ViewIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
                      <Typography variant="caption" color="text.secondary">
                        {formatNumber(video.viewCount)}
                      </Typography>
                    </Box>
                  )}
                  {video.likeCount && (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <LikeIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
                      <Typography variant="caption" color="text.secondary">
                        {formatNumber(video.likeCount)}
                      </Typography>
                    </Box>
                  )}
                </Box>

                {/* 업로드 날짜 */}
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, mb: 1 }}>
                  <TimeIcon sx={{ fontSize: 14, color: 'text.secondary' }} />
                  <Typography variant="caption" color="text.secondary">
                    {formatDate(video.uploadDate)}
                  </Typography>
                </Box>

                {/* 방문 국가들 */}
                {video.visitCountries && video.visitCountries.length > 0 && (
                  <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
                    {video.visitCountries.slice(0, 3).map((country) => (
                      <Chip
                        key={country.id}
                        label={`${country.countryEmoji} ${country.countryName}`}
                        size="small"
                        variant="outlined"
                        sx={{ fontSize: '0.7rem', height: 20 }}
                      />
                    ))}
                    {video.visitCountries.length > 3 && (
                      <Chip
                        label={`+${video.visitCountries.length - 3}`}
                        size="small"
                        variant="outlined"
                        sx={{ fontSize: '0.7rem', height: 20 }}
                      />
                    )}
                  </Box>
                )}
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      {/* 더 보기 버튼 */}
      {hasMore && (
        <Box sx={{ display: 'flex', justifyContent: 'center', mt: 3 }}>
          <Button
            variant="outlined"
            onClick={onLoadMore}
            disabled={loading}
            startIcon={loading ? <CircularProgress size={16} /> : undefined}
          >
            {loading ? '로딩 중...' : '더 보기'}
          </Button>
        </Box>
      )}
    </Box>
  );
};

export default VideoList; 