import React, { useEffect, useState } from 'react';
import {
  Container,
  Typography,
  Box,
  CssBaseline,
  ThemeProvider,
  createTheme,
  AppBar,
  Toolbar,
  Alert,
  Snackbar,
  CircularProgress,
} from '@mui/material';
import { Provider } from 'react-redux';
import { store } from './store';
import { useAppDispatch, useAppSelector } from './store';
import {
  fetchFilterOptions,
  fetchMapData,
  fetchVideos,
  updateFilters,
  resetFilters,
  clearError,
  setSelectedVideo,
} from './store/appSlice';
import FilterPanel from './components/FilterPanel';
import WorldMap from './components/WorldMap';
import VideoList from './components/VideoList';
import { FilterState, Video } from './types';

// Material-UI 테마 설정
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2',
    },
    secondary: {
      main: '#dc004e',
    },
  },
  typography: {
    h4: {
      fontWeight: 600,
    },
    h6: {
      fontWeight: 500,
    },
  },
});

// 메인 컴포넌트 (Redux 연결)
const MainApp: React.FC = () => {
  const dispatch = useAppDispatch();
  const {
    filters,
    filterOptions,
    mapData,
    videos,
    selectedVideo,
    loading,
    error,
  } = useAppSelector((state) => state.app);

  const [showError, setShowError] = useState(false);

  // 초기 데이터 로딩
  useEffect(() => {
    dispatch(fetchFilterOptions());
  }, [dispatch]);

  // 필터가 적용될 때마다 지도 데이터 업데이트
  useEffect(() => {
    dispatch(fetchMapData(filters));
    dispatch(fetchVideos({ filters }));
  }, [dispatch, filters]);

  // 에러 표시
  useEffect(() => {
    if (error) {
      setShowError(true);
    }
  }, [error]);

  // 필터 변경 핸들러
  const handleFilterChange = (newFilters: FilterState) => {
    dispatch(updateFilters(newFilters));
  };

  // 필터 적용 핸들러
  const handleApplyFilters = () => {
    dispatch(fetchMapData(filters));
    dispatch(fetchVideos({ filters }));
  };

  // 필터 리셋 핸들러
  const handleResetFilters = () => {
    dispatch(resetFilters());
  };

  // 국가 클릭 핸들러
  const handleCountryClick = (countryCode: string) => {
    const newFilters = { ...filters, selectedCountryCode: countryCode };
    dispatch(updateFilters(newFilters));
  };

  // 영상 클릭 핸들러
  const handleVideoClick = (video: Video) => {
    dispatch(setSelectedVideo(video));
    // YouTube 링크로 이동
    window.open(video.videoUrl, '_blank');
  };

  // 에러 닫기 핸들러
  const handleCloseError = () => {
    setShowError(false);
    dispatch(clearError());
  };

  return (
    <Box sx={{ flexGrow: 1 }}>
      {/* 앱 바 */}
      <AppBar position="static" elevation={1}>
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            🗺️ TravelMap - 유튜버 여행 지도
          </Typography>
          <Typography variant="body2" sx={{ opacity: 0.8 }}>
            유튜버들의 여행 영상을 지도에서 확인해보세요
          </Typography>
        </Toolbar>
      </AppBar>

      <Container maxWidth="xl" sx={{ mt: 3, mb: 3 }}>
        {/* 필터 패널 */}
        {filterOptions && (
          <FilterPanel
            options={filterOptions}
            filters={filters}
            onFilterChange={handleFilterChange}
            onApplyFilters={handleApplyFilters}
            onResetFilters={handleResetFilters}
          />
        )}

        {/* 지도 영역 */}
        <Box sx={{ mb: 4 }}>
          <Typography variant="h5" sx={{ mb: 2, fontWeight: 600 }}>
            🌍 세계 여행 지도
          </Typography>
          
          {loading && !mapData ? (
            <Box 
              sx={{ 
                height: 500, 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center',
                border: '1px solid #e0e0e0',
                borderRadius: 1,
              }}
            >
              <CircularProgress />
            </Box>
          ) : mapData ? (
            <WorldMap
              mapData={mapData}
              onCountryClick={handleCountryClick}
            />
          ) : (
            <Box 
              sx={{ 
                height: 500, 
                display: 'flex', 
                alignItems: 'center', 
                justifyContent: 'center',
                border: '1px solid #e0e0e0',
                borderRadius: 1,
                backgroundColor: '#f5f5f5',
              }}
            >
              <Typography color="text.secondary">
                지도 데이터를 불러오는 중입니다...
              </Typography>
            </Box>
          )}
        </Box>

        {/* 영상 리스트 영역 */}
        <Box>
          <Typography variant="h5" sx={{ mb: 2, fontWeight: 600 }}>
            📹 여행 영상 목록
          </Typography>
          
          <VideoList
            videos={videos}
            onVideoClick={handleVideoClick}
            loading={loading}
            error={error}
          />
        </Box>

        {/* 통계 정보 */}
        {mapData && (
          <Box 
            sx={{ 
              mt: 4, 
              p: 3, 
              backgroundColor: '#f8f9fa', 
              borderRadius: 2,
              textAlign: 'center',
            }}
          >
            <Typography variant="h6" sx={{ mb: 2 }}>
              📊 여행 통계
            </Typography>
            <Box sx={{ display: 'flex', justifyContent: 'center', gap: 4, flexWrap: 'wrap' }}>
              <Box>
                <Typography variant="h4" color="primary" fontWeight="bold">
                  {mapData.countries.length}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  방문 국가
                </Typography>
              </Box>
              <Box>
                <Typography variant="h4" color="primary" fontWeight="bold">
                  {mapData.countries.reduce((sum, country) => sum + country.visitCount, 0)}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  총 방문 횟수
                </Typography>
              </Box>
              <Box>
                <Typography variant="h4" color="primary" fontWeight="bold">
                  {videos.length}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  여행 영상
                </Typography>
              </Box>
            </Box>
          </Box>
        )}
      </Container>

      {/* 에러 스낵바 */}
      <Snackbar
        open={showError}
        autoHideDuration={6000}
        onClose={handleCloseError}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={handleCloseError} severity="error" sx={{ width: '100%' }}>
          {error}
        </Alert>
      </Snackbar>
    </Box>
  );
};

// App 컴포넌트 (Provider 래핑)
const App: React.FC = () => {
  return (
    <Provider store={store}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <MainApp />
      </ThemeProvider>
    </Provider>
  );
};

export default App;
