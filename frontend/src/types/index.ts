// User Types
export interface User {
  id: number;
  name: string;
  searchQuery?: string;
  youtubeChannelId: string;
  channelUrl: string;
  profileImageUrl?: string;
  subscriberCount?: number;
  totalVideoCount?: number;
}

// Video Types
export interface Video {
  id: number;
  title: string;
  videoId: string;
  uploadDate?: string;
  thumbnailUrl?: string;
  videoUrl: string;
  viewCount?: number;
  likeCount?: number;
  duration?: string;
  user: User;
  visitCountries: VisitCountry[];
}

// Visit Country Types
export interface VisitCountry {
  id: number;
  countryCode: string;
  countryName: string;
  countryEmoji?: string;
  continent?: string;
  cityName?: string;
  cityLatitude?: number;
  cityLongitude?: number;
  detectionMethod: string;
  confidenceScore?: number;
  visitOrder?: number;
}

// Country/City Keyword Types (영상 제목에서 국가/도시를 감지할 때 쓰는 키워드 - 관리자 API로 추가/삭제)
export interface CountryKeyword {
  id: number;
  keyword: string;
  countryCode: string;
  countryName: string;
  continent?: string;
  countryEmoji?: string;
}

export interface CityKeyword {
  id: number;
  keyword: string;
  cityName: string;
  latitude: number;
  longitude: number;
  countryCode: string;
  countryName: string;
  continent?: string;
  countryEmoji?: string;
}

// Filter Options Types
export interface FilterOptions {
  users: User[];
  countries: string[];
  years: string[];
  continents: string[];
}

// Map Data Types
export interface CountryData {
  countryCode: string;
  countryName: string;
  countryEmoji?: string;
  continent?: string;
  visitCount: number;
  youtuberCount: number;
  youtubers: User[];
}

export interface MapData {
  countries: CountryData[];
}

// Filter State Types
export interface FilterState {
  selectedUserId?: number;
  selectedCountryCode?: string;
  selectedYear?: string;
  selectedContinent?: string;
  startDate?: string;
  endDate?: string;
}

// API Response Types
export interface ApiResponse<T> {
  data: T;
  status: number;
  message?: string;
}

// Redux State Types
export interface AppState {
  filters: FilterState;
  filterOptions: FilterOptions | null;
  mapData: MapData | null;
  videos: Video[];
  videosPage: number;
  hasMoreVideos: boolean;
  selectedVideo: Video | null;
  loading: boolean;
  error: string | null;
}

// Component Props Types
export interface MapComponentProps {
  data: MapData;
  onCountryClick: (countryCode: string) => void;
}

export interface FilterComponentProps {
  options: FilterOptions;
  filters: FilterState;
  onFilterChange: (filters: FilterState) => void;
}

export interface VideoListProps {
  videos: Video[];
  onVideoClick: (video: Video) => void;
  loading?: boolean;
}

export interface VideoCardProps {
  video: Video;
  onClick: (video: Video) => void;
} 