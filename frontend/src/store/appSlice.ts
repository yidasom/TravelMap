import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { AppState, FilterState, Video } from '../types';
import { apiService } from '../services/api';

// 초기 상태
const initialState: AppState = {
  filters: {},
  filterOptions: null,
  mapData: null,
  videos: [],
  selectedVideo: null,
  loading: false,
  error: null,
};

// 비동기 액션들
export const fetchFilterOptions = createAsyncThunk(
  'app/fetchFilterOptions',
  async () => {
    return await apiService.getFilterOptions();
  }
);

export const fetchMapData = createAsyncThunk(
  'app/fetchMapData',
  async (filters: FilterState) => {
    return await apiService.getMapData(filters);
  }
);

export const fetchVideos = createAsyncThunk(
  'app/fetchVideos',
  async ({ filters, page = 0, size = 20 }: { filters: FilterState; page?: number; size?: number }) => {
    return await apiService.getVideos(filters, page, size);
  }
);

export const fetchVideo = createAsyncThunk(
  'app/fetchVideo',
  async (id: number) => {
    return await apiService.getVideo(id);
  }
);

export const fetchVideosByCountry = createAsyncThunk(
  'app/fetchVideosByCountry',
  async (countryCode: string) => {
    return await apiService.getVideosByCountry(countryCode);
  }
);

// 슬라이스 생성
const appSlice = createSlice({
  name: 'app',
  initialState,
  reducers: {
    // 필터 업데이트
    updateFilters: (state, action: PayloadAction<Partial<FilterState>>) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    
    // 필터 리셋
    resetFilters: (state) => {
      state.filters = {};
    },
    
    // 에러 클리어
    clearError: (state) => {
      state.error = null;
    },
    
    // 선택된 비디오 설정
    setSelectedVideo: (state, action: PayloadAction<Video | null>) => {
      state.selectedVideo = action.payload;
    },
    
    // 비디오 목록 리셋
    resetVideos: (state) => {
      state.videos = [];
    },
  },
  extraReducers: (builder) => {
    // fetchFilterOptions
    builder
      .addCase(fetchFilterOptions.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchFilterOptions.fulfilled, (state, action) => {
        state.loading = false;
        state.filterOptions = action.payload;
      })
      .addCase(fetchFilterOptions.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '필터 옵션을 불러오는 중 오류가 발생했습니다.';
      });

    // fetchMapData
    builder
      .addCase(fetchMapData.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchMapData.fulfilled, (state, action) => {
        state.loading = false;
        state.mapData = action.payload;
      })
      .addCase(fetchMapData.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '지도 데이터를 불러오는 중 오류가 발생했습니다.';
      });

    // fetchVideos
    builder
      .addCase(fetchVideos.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchVideos.fulfilled, (state, action) => {
        state.loading = false;
        state.videos = action.payload;
      })
      .addCase(fetchVideos.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '영상 목록을 불러오는 중 오류가 발생했습니다.';
      });

    // fetchVideo
    builder
      .addCase(fetchVideo.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchVideo.fulfilled, (state, action) => {
        state.loading = false;
        state.selectedVideo = action.payload;
      })
      .addCase(fetchVideo.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '영상 정보를 불러오는 중 오류가 발생했습니다.';
      });

    // fetchVideosByCountry
    builder
      .addCase(fetchVideosByCountry.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchVideosByCountry.fulfilled, (state, action) => {
        state.loading = false;
        state.videos = action.payload;
      })
      .addCase(fetchVideosByCountry.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || '국가별 영상을 불러오는 중 오류가 발생했습니다.';
      });
  },
});

export const {
  updateFilters,
  resetFilters,
  clearError,
  setSelectedVideo,
  resetVideos,
} = appSlice.actions;

export default appSlice.reducer; 