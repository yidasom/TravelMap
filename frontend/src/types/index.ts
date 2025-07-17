// User Types
export interface User {
  id: number;
  name: string;
  gender?: string;
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
  detectionMethod: string;
  confidenceScore?: number;
  visitOrder?: number;
}

// Filter Options Types
export interface FilterOptions {
  users: User[];
  countries: string[];
  genders: string[];
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
  selectedGender?: string;
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