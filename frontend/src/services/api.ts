import axios from 'axios';
import { FilterOptions, MapData, Video, FilterState } from '../types';

// API 기본 설정
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// API 서비스 함수들
export const apiService = {
  // 필터 옵션 조회
  async getFilterOptions(): Promise<FilterOptions> {
    const response = await api.get<FilterOptions>('/filters');
    return response.data;
  },

  // 지도 데이터 조회
  async getMapData(filters: FilterState): Promise<MapData> {
    const params = new URLSearchParams();
    
    if (filters.selectedUserId) {
      params.append('userId', filters.selectedUserId.toString());
    }
    if (filters.selectedCountryCode) {
      params.append('countryCode', filters.selectedCountryCode);
    }
    if (filters.selectedGender) {
      params.append('gender', filters.selectedGender);
    }
    if (filters.startDate) {
      params.append('startDate', filters.startDate);
    }
    if (filters.endDate) {
      params.append('endDate', filters.endDate);
    }

    const response = await api.get<MapData>(`/map-data?${params.toString()}`);
    return response.data;
  },

  // 영상 목록 조회
  async getVideos(filters: FilterState, page: number = 0, size: number = 20): Promise<Video[]> {
    const params = new URLSearchParams();
    
    if (filters.selectedUserId) {
      params.append('userId', filters.selectedUserId.toString());
    }
    if (filters.selectedCountryCode) {
      params.append('countryCode', filters.selectedCountryCode);
    }
    if (filters.selectedGender) {
      params.append('gender', filters.selectedGender);
    }
    if (filters.startDate) {
      params.append('startDate', filters.startDate);
    }
    if (filters.endDate) {
      params.append('endDate', filters.endDate);
    }
    
    params.append('page', page.toString());
    params.append('size', size.toString());

    const response = await api.get<Video[]>(`/videos?${params.toString()}`);
    return response.data;
  },

  // 개별 영상 상세 조회
  async getVideo(id: number): Promise<Video> {
    const response = await api.get<Video>(`/videos/${id}`);
    return response.data;
  },

  // 특정 국가의 영상 목록 조회
  async getVideosByCountry(countryCode: string): Promise<Video[]> {
    const response = await api.get<Video[]>(`/countries/${countryCode}/videos`);
    return response.data;
  },
};

// 에러 핸들링을 위한 인터셉터
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    
    if (error.response) {
      // 서버가 응답했지만 상태 코드가 2xx가 아닌 경우
      const message = error.response.data?.message || `HTTP Error: ${error.response.status}`;
      throw new Error(message);
    } else if (error.request) {
      // 요청이 전송되었지만 응답을 받지 못한 경우
      throw new Error('네트워크 오류: 서버와 연결할 수 없습니다.');
    } else {
      // 요청 설정 중 오류가 발생한 경우
      throw new Error(`요청 오류: ${error.message}`);
    }
  }
);

export default api; 