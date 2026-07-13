import React, { useState } from 'react';
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
  ToggleButton,
  ToggleButtonGroup,
} from '@mui/material';
import {
  PlayArrow as PlayIcon,
  Visibility as ViewIcon,
  ThumbUp as LikeIcon,
  AccessTime as TimeIcon,
  ViewModule as GridViewIcon,
  ViewList as ListViewIcon,
} from '@mui/icons-material';
import { Video } from '../types';

type ViewMode = 'grid' | 'list';

interface VideoListProps {
  videos: Video[];
  onVideoClick: (video: Video) => void;
  loading?: boolean;
  error?: string | null;
  onLoadMore?: () => void;
  hasMore?: boolean;
}

const VIEW_MODE_STORAGE_KEY = 'travelmap.videoListViewMode';

const VideoList: React.FC<VideoListProps> = ({
  videos,
  onVideoClick,
  loading = false,
  error = null,
  onLoadMore,
  hasMore = false,
}) => {
  const [viewMode, setViewMode] = useState<ViewMode>(() => {
    const stored = localStorage.getItem(VIEW_MODE_STORAGE_KEY);
    return stored === 'list' ? 'list' : 'grid';
  });

  const handleViewModeChange = (_: React.MouseEvent<HTMLElement>, newMode: ViewMode | null) => {
    if (newMode) {
      setViewMode(newMode);
      localStorage.setItem(VIEW_MODE_STORAGE_KEY, newMode);
    }
  };

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

  const renderCountryChips = (video: Video, maxVisible: number) => {
    if (!video.visitCountries || video.visitCountries.length === 0) return null;
    return (
      <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
        {video.visitCountries.slice(0, maxVisible).map((country) => (
          <Chip
            key={country.id}
            label={`${country.countryEmoji} ${country.countryName}${country.cityName ? ' · ' + country.cityName : ''}`}
            size="small"
            variant="outlined"
            sx={{ fontSize: '0.7rem', height: 20 }}
          />
        ))}
        {video.visitCountries.length > maxVisible && (
          <Chip
            label={`+${video.visitCountries.length - maxVisible}`}
            size="small"
            variant="outlined"
            sx={{ fontSize: '0.7rem', height: 20 }}
          />
        )}
      </Box>
    );
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
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: 2 }}>
        <Typography variant="h6">
          영상 목록 ({videos.length}개)
        </Typography>
        <ToggleButtonGroup
          value={viewMode}
          exclusive
          onChange={handleViewModeChange}
          size="small"
        >
          <ToggleButton value="grid" aria-label="썸네일형">
            <GridViewIcon fontSize="small" sx={{ mr: 0.5 }} />
            썸네일형
          </ToggleButton>
          <ToggleButton value="list" aria-label="목록형">
            <ListViewIcon fontSize="small" sx={{ mr: 0.5 }} />
            목록형
          </ToggleButton>
        </ToggleButtonGroup>
      </Box>

      {viewMode === 'grid' ? (
        <Grid container spacing={2}>
          {videos.map((video) => (
            <Grid item xs={12} sm={6} md={4} lg={3} key={video.id}>
              <Card
                sx={{
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
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
                <Box sx={{ position: 'relative', flexShrink: 0 }}>
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

                <CardContent sx={{ pb: 1, flexGrow: 1, display: 'flex', flexDirection: 'column' }}>
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
                  </Box>

                  {/* 통계 정보 */}
                  <Box sx={{ display: 'flex', gap: 2, mb: 1, minHeight: 20 }}>
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

                  {/* 방문 국가들 (카드 하단에 고정되도록 mt: auto) */}
                  <Box sx={{ mt: 'auto', pt: video.visitCountries?.length ? 1 : 0 }}>
                    {renderCountryChips(video, 3)}
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          ))}
        </Grid>
      ) : (
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 1.5 }}>
          {videos.map((video) => (
            <Card
              key={video.id}
              sx={{
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'stretch',
                transition: 'box-shadow 0.2s',
                '&:hover': { boxShadow: 4 },
              }}
              onClick={() => onVideoClick(video)}
            >
              {/* 썸네일 */}
              <Box sx={{ position: 'relative', flexShrink: 0, width: { xs: 120, sm: 180 } }}>
                <CardMedia
                  component="img"
                  image={video.thumbnailUrl || '/placeholder-thumbnail.png'}
                  alt={video.title}
                  sx={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
                {video.duration && (
                  <Chip
                    label={formatDuration(video.duration)}
                    size="small"
                    sx={{
                      position: 'absolute',
                      bottom: 4,
                      right: 4,
                      backgroundColor: 'rgba(0, 0, 0, 0.7)',
                      color: 'white',
                      fontWeight: 'bold',
                      height: 18,
                      fontSize: '0.65rem',
                    }}
                  />
                )}
              </Box>

              <CardContent sx={{ flex: 1, minWidth: 0, py: 1, '&:last-child': { pb: 1 } }}>
                <Typography
                  variant="subtitle2"
                  sx={{
                    fontWeight: 'bold',
                    lineHeight: 1.3,
                    overflow: 'hidden',
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical',
                    mb: 0.5,
                  }}
                  title={video.title}
                >
                  {video.title}
                </Typography>

                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 0.5, flexWrap: 'wrap' }}>
                  <Avatar
                    src={video.user.profileImageUrl}
                    alt={video.user.name}
                    sx={{ width: 20, height: 20 }}
                  />
                  <Typography variant="caption" color="text.secondary">
                    {video.user.name}
                  </Typography>

                  {video.viewCount != null && (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5, ml: 1 }}>
                      <ViewIcon sx={{ fontSize: 13, color: 'text.secondary' }} />
                      <Typography variant="caption" color="text.secondary">
                        {formatNumber(video.viewCount)}
                      </Typography>
                    </Box>
                  )}
                  {video.likeCount != null && (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                      <LikeIcon sx={{ fontSize: 13, color: 'text.secondary' }} />
                      <Typography variant="caption" color="text.secondary">
                        {formatNumber(video.likeCount)}
                      </Typography>
                    </Box>
                  )}
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                    <TimeIcon sx={{ fontSize: 13, color: 'text.secondary' }} />
                    <Typography variant="caption" color="text.secondary">
                      {formatDate(video.uploadDate)}
                    </Typography>
                  </Box>
                </Box>

                {renderCountryChips(video, 6)}
              </CardContent>
            </Card>
          ))}
        </Box>
      )}

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
