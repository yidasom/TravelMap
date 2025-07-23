import React, { useEffect, useState, useRef } from 'react';
import { MapContainer, TileLayer, GeoJSON, Marker, Polyline, useMap } from 'react-leaflet';
import { Box, Typography, CircularProgress, Backdrop, IconButton } from '@mui/material';
import { MapData, CountryData } from '../types';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css';

interface WorldMapProps {
  mapData: MapData;
  onCountryClick: (countryCode: string) => void;
}

// 세계 지도 GeoJSON URL
const geoUrl = "https://raw.githubusercontent.com/holtzy/D3-graph-gallery/master/DATA/world.geojson";

// 클러스터 마커 생성 함수
const createClusterIcon = (count: number, youtubers: any[]) => {
  const size = Math.min(40 + count * 5, 60);
  const color = count === 1 ? '#ff4444' : count <= 3 ? '#ff8800' : '#ff0000';
  
  return new L.DivIcon({
    className: 'cluster-marker',
    html: `
      <div style="
        background-color: ${color};
        border: 3px solid white;
        border-radius: 50%;
        width: ${size}px;
        height: ${size}px;
        display: flex;
        align-items: center;
        justify-content: center;
        box-shadow: 0 4px 8px rgba(0,0,0,0.3);
        font-weight: bold;
        color: white;
        font-size: ${count > 10 ? '12px' : '14px'};
      ">
        ${count > 1 ? count : '●'}
      </div>
    `,
    iconSize: [size, size],
    iconAnchor: [size/2, size/2]
  });
};

// 국가 좌표 매핑
const countryCoordinates: { [key: string]: [number, number] } = {
  'KR': [36.5, 127.5], 'JP': [36, 138], 'CN': [35, 105], 'US': [40, -100],
  'GB': [54, -3], 'FR': [46, 2], 'DE': [51, 9], 'IT': [42, 12],
  'ES': [40, -4], 'RU': [60, 100], 'CA': [60, -95], 'AU': [-25, 133],
  'BR': [-14, -51], 'IN': [20, 77], 'TH': [15, 100], 'VN': [16, 108],
  'SG': [1.35, 103.8], 'MY': [4.2, 101.9], 'ID': [-0.8, 113.9],
  'PH': [13, 122], 'TR': [39, 35], 'EG': [26, 30], 'ZA': [-29, 24],
  'MX': [23, -102], 'AR': [-34, -64], 'CL': [-30, -71], 'PE': [-10, -76],
  'NL': [52.3, 5.7], 'BE': [50.8, 4.3], 'CH': [46.8, 8.2], 'AT': [47.5, 14.5],
  'SE': [59.3, 18.1], 'NO': [60.5, 8.5], 'DK': [55.7, 12.6], 'FI': [64.9, 26],
  'PL': [51.9, 19.1], 'CZ': [49.8, 15.5], 'HU': [47.2, 19.5], 'PT': [39.4, -8.2]
};

// 안전한 국가 좌표 가져오기 (대한민국을 기본값으로 사용)
const getCountryCoordinates = (countryCode: string): [number, number] => {
  // 국가 코드가 비어있거나 유효하지 않은 경우 대한민국 사용
  if (!countryCode || typeof countryCode !== 'string') {
    console.warn(`Invalid country code: ${countryCode}, using Korea as fallback`);
    return countryCoordinates['KR'];
  }
  
  // 해당 국가의 좌표가 있으면 반환, 없으면 대한민국 좌표 반환
  const coordinates = countryCoordinates[countryCode.toUpperCase()];
  if (coordinates) {
    return coordinates;
  } else {
    console.warn(`Country coordinates not found for: ${countryCode}, using Korea as fallback`);
    return countryCoordinates['KR'];
  }
};

// 지도 바운딩 및 반응형 처리 컴포넌트
const MapBounds: React.FC<{ countries: CountryData[] }> = ({ countries }) => {
  const map = useMap();
  
  useEffect(() => {
    if (countries.length > 0) {
      const bounds = countries.map(country => {
        const coords = getCountryCoordinates(country.countryCode);
        return L.latLng(coords[0], coords[1]);
      });
      
      if (bounds.length > 0) {
        const group = L.featureGroup(bounds.map(coord => L.marker(coord)));
        const boundsObj = group.getBounds();
        
        // 스마트한 패딩 계산 (지역 크기에 따라 동적 조정)
        const boundsWidth = boundsObj.getEast() - boundsObj.getWest();
        const boundsHeight = boundsObj.getNorth() - boundsObj.getSouth();
        const maxDimension = Math.max(boundsWidth, boundsHeight);
        
        // 더 관대한 패딩 설정 (최소 3배, 최대 5배 확장)
        let paddingMultiplier = 3.0;
        if (maxDimension < 30) paddingMultiplier = 5.0; // 작은 지역은 더 넓게
        else if (maxDimension < 60) paddingMultiplier = 4.0;
        else if (maxDimension < 120) paddingMultiplier = 3.5;
        
        const extendedBounds = boundsObj.pad(paddingMultiplier);
        
        // 전세계 경계를 넘지 않도록 제한
        const worldBounds = L.latLngBounds(
          L.latLng(-85, -180),
          L.latLng(85, 180)
        );
        
        // 확장된 경계가 전세계를 넘지 않도록 클램핑
        const finalBounds = L.latLngBounds(
          L.latLng(
            Math.max(extendedBounds.getSouth(), worldBounds.getSouth()),
            Math.max(extendedBounds.getWest(), worldBounds.getWest())
          ),
          L.latLng(
            Math.min(extendedBounds.getNorth(), worldBounds.getNorth()),
            Math.min(extendedBounds.getEast(), worldBounds.getEast())
          )
        );
        
        // 지도 이동 범위 제한 설정
        map.setMaxBounds(finalBounds);
        
        // 초기 위치 설정 (더 여유로운 뷰)
        map.fitBounds(boundsObj.pad(0.4), {
          maxZoom: 5, // 최대 줌을 약간 낮춰서 더 넓은 시야 확보
          animate: true,
          duration: 1.5,
          padding: [60, 60]
        });
      }
    } else {
      // 데이터가 없으면 대한민국 중심으로 설정
      const koreaCoords = getCountryCoordinates('KR');
      map.setView(koreaCoords, 7);
      
      // 전세계 뷰도 허용
      const worldBounds = L.latLngBounds(
        L.latLng(-85, -180),
        L.latLng(85, 180)
      );
      map.setMaxBounds(worldBounds);
    }
  }, [countries, map]);

  // 줌 레벨 변화에 따른 동적 경계 조정
  useEffect(() => {
    let originalBounds: L.LatLngBounds | null = null;
    
    const handleZoomEnd = () => {
      const currentZoom = map.getZoom();
      
      // 줌 레벨이 높을 때 (확대 시) 경계를 더 유연하게 조정
      if (currentZoom >= 6 && countries.length > 0) {
        // 고줌 레벨에서는 경계 제한을 더 완화
        const bounds = countries.map(country => {
          const coords = getCountryCoordinates(country.countryCode);
          return L.latLng(coords[0], coords[1]);
        });
        
        if (bounds.length > 0) {
          const group = L.featureGroup(bounds.map(coord => L.marker(coord)));
          const boundsObj = group.getBounds();
          
          // 고줌에서는 더 넓은 범위 허용
          const expandedBounds = boundsObj.pad(6.0);
          const worldBounds = L.latLngBounds(L.latLng(-85, -180), L.latLng(85, 180));
          
          const finalBounds = L.latLngBounds(
            L.latLng(
              Math.max(expandedBounds.getSouth(), worldBounds.getSouth()),
              Math.max(expandedBounds.getWest(), worldBounds.getWest())
            ),
            L.latLng(
              Math.min(expandedBounds.getNorth(), worldBounds.getNorth()),
              Math.min(expandedBounds.getEast(), worldBounds.getEast())
            )
          );
          
          map.setMaxBounds(finalBounds);
        }
      }
    };

    map.on('zoomend', handleZoomEnd);
    
    return () => {
      map.off('zoomend', handleZoomEnd);
    };
  }, [map, countries]);

  // 지도 컨테이너 크기 변경 시 반응형 처리
  useEffect(() => {
    const handleResize = () => {
      setTimeout(() => {
        map.invalidateSize();
      }, 200);
    };
    
    window.addEventListener('resize', handleResize);
    
    // ResizeObserver로 지도 컨테이너의 부모 요소 크기 변화 감지
    let resizeObserver: ResizeObserver | null = null;
    const mapContainer = map.getContainer().parentElement;
    
    if (mapContainer && window.ResizeObserver) {
      resizeObserver = new ResizeObserver(() => {
        handleResize();
      });
      resizeObserver.observe(mapContainer);
    }
    
    return () => {
      window.removeEventListener('resize', handleResize);
      if (resizeObserver) {
        resizeObserver.disconnect();
      }
    };
  }, [map]);
  
  return null;
};

const WorldMap: React.FC<WorldMapProps> = ({ mapData, onCountryClick }) => {
  const [geoData, setGeoData] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [mapReady, setMapReady] = useState(false);
  const [showLegend, setShowLegend] = useState(false);
  const [showStats, setShowStats] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
  const mapRef = useRef<any>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  // 국가별 데이터를 코드로 매핑
  const countryDataMap = new Map<string, CountryData>();
  mapData.countries.forEach(country => {
    countryDataMap.set(country.countryCode, country);
  });

  // 클러스터링된 마커 데이터 생성
  const clusterData = React.useMemo(() => {
    const clusters = new Map<string, {
      position: [number, number];
      countries: CountryData[];
      totalVisits: number;
    }>();

    mapData.countries.forEach(country => {
      const coords = getCountryCoordinates(country.countryCode);
      const key = `${Math.round(coords[0] * 2) / 2}_${Math.round(coords[1] * 2) / 2}`;
      
      if (!clusters.has(key)) {
        clusters.set(key, {
          position: coords,
          countries: [],
          totalVisits: 0
        });
      }
      
      const cluster = clusters.get(key)!;
      cluster.countries.push(country);
      cluster.totalVisits += country.visitCount;
    });

    return Array.from(clusters.values());
  }, [mapData]);

  // 유튜버별 여행 경로 생성
  const travelRoutes = React.useMemo(() => {
    const routes: { [userId: number]: [number, number][] } = {};
    
    mapData.countries.forEach(country => {
      country.youtubers.forEach(youtuber => {
        if (!routes[youtuber.id]) {
          routes[youtuber.id] = [];
        }
        const coords = getCountryCoordinates(country.countryCode);
        if (!routes[youtuber.id].some(([lat, lng]) => lat === coords[0] && lng === coords[1])) {
          routes[youtuber.id].push(coords);
        }
      });
    });

    return Object.entries(routes)
      .filter(([_, path]) => path.length >= 2)
      .map(([userId, path], index) => ({
        userId: parseInt(userId),
        path,
        color: `hsl(${index * 60}, 70%, 50%)`
      }));
  }, [mapData]);

  // GeoJSON 데이터 로드 및 반응형 초기화
  useEffect(() => {
    setIsLoading(true);
    fetch(geoUrl)
      .then(response => response.json())
      .then(data => {
        setGeoData(data);
        setIsLoading(false);
        setTimeout(() => {
          setMapReady(true);
          // 지도 크기 재조정
          if (mapRef.current) {
            mapRef.current.invalidateSize();
          }
        }, 500);
      })
      .catch(error => {
        console.error('Error loading geo data:', error);
        setIsLoading(false);
      });
  }, []);

  // 컨테이너 크기 변화 감지 및 반응형 처리
  useEffect(() => {
    const handleContainerResize = () => {
      if (mapRef.current) {
        setTimeout(() => {
          mapRef.current.invalidateSize();
        }, 100);
      }
    };

    let resizeObserver: ResizeObserver | null = null;
    
    if (containerRef.current && window.ResizeObserver) {
      resizeObserver = new ResizeObserver(handleContainerResize);
      resizeObserver.observe(containerRef.current);
    }

    return () => {
      if (resizeObserver) {
        resizeObserver.disconnect();
      }
    };
  }, [geoData]);

  // 화면 크기 변화 감지
  useEffect(() => {
    const handleResize = () => {
      const mobile = window.innerWidth <= 768;
      setIsMobile(mobile);
      
      // 데스크탑에서는 기본적으로 표시, 모바일에서는 숨김
      if (!mobile) {
        setShowLegend(true);
        setShowStats(true);
      } else {
        setShowLegend(false);
        setShowStats(false);
      }
    };

    // 초기 설정
    handleResize();
    
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  // 방문 횟수에 따른 색상 계산
  const getCountryColor = (countryCode: string): string => {
    const countryData = countryDataMap.get(countryCode);
    if (!countryData) {
      return '#f0f0f0';
    }

    const visitCount = countryData.visitCount;
    
    if (visitCount >= 20) return '#0d47a1';
    if (visitCount >= 10) return '#1976d2';
    if (visitCount >= 5) return '#42a5f5';
    if (visitCount >= 1) return '#90caf9';
    
    return '#f0f0f0';
  };

  // GeoJSON 스타일 함수
  const geoJsonStyle = (feature: any) => {
    const countryCode = feature.properties.ISO_A2;
    const countryData = countryDataMap.get(countryCode);
    
    return {
      fillColor: getCountryColor(countryCode),
      weight: countryData ? 2 : 0.5,
      opacity: 1,
      color: countryData ? '#2c3e50' : '#bdc3c7',
      fillOpacity: countryData ? 0.85 : 0.2,
      dashArray: countryData ? '' : '3,3',
    };
  };

  // 국가 클릭 이벤트 핸들러
  const onEachFeature = (feature: any, layer: any) => {
    const countryCode = feature.properties.ISO_A2;
    const countryData = countryDataMap.get(countryCode);

    layer.on({
      click: (e: any) => {
        if (countryData) {
          onCountryClick(countryCode);
        }
      }
    });
  };

  return (
    <Box 
      ref={containerRef}
      sx={{ 
        width: '100%', 
        height: { xs: '50vh', sm: '60vh', md: '70vh', lg: '600px' },
        minHeight: { xs: '300px', sm: '400px', md: '500px' },
        position: 'relative',
        display: 'flex',
        flexDirection: 'column'
      }}
    >
      {/* 로딩 오버레이 */}
      <Backdrop
        sx={{
          position: 'absolute',
          zIndex: 1000,
          color: '#fff',
          backgroundColor: 'rgba(255, 255, 255, 0.8)',
          borderRadius: 1
        }}
        open={isLoading}
      >
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2 }}>
          <CircularProgress color="primary" size={60} />
          <Typography variant="h6" sx={{ fontWeight: 'normal' }}>
            🗺️ 지도 데이터 로딩 중...
          </Typography>
        </Box>
      </Backdrop>

      {/* 지도 로딩 완료 메시지 */}
      {mapReady && (
        <Box
          sx={{
            position: 'absolute',
            top: 16,
            left: '50%',
            transform: 'translateX(-50%)',
            backgroundColor: 'rgba(76, 175, 80, 0.9)',
            color: 'white',
            padding: '8px 16px',
            borderRadius: 2,
            zIndex: 1002,
            animation: 'fadeOut 3s forwards',
            '@keyframes fadeOut': {
              '0%': { opacity: 1 },
              '70%': { opacity: 1 },
              '100%': { opacity: 0 }
            }
          }}
        >
          <Typography variant="body2" sx={{ fontWeight: 'normal' }}>
            ✓ 지도 로딩 완료
          </Typography>
        </Box>
      )}

      <MapContainer
        center={[20, 0]}
        zoom={2}
        style={{ 
          height: '100%', 
          width: '100%', 
          position: 'relative', 
          zIndex: 1,
          minHeight: '400px'
        }}
        scrollWheelZoom={true}
        dragging={true}
        touchZoom={'center'}
        doubleClickZoom={'center'}
        boxZoom={false}
        keyboard={true}
        ref={mapRef}
        maxZoom={10}
        minZoom={2}
        maxBoundsViscosity={0.6}
        wheelPxPerZoomLevel={120}
        bounceAtZoomLimits={true}
        zoomControl={true}
        attributionControl={true}
        className="responsive-map"
        whenReady={() => {
          // 지도가 완전히 로드된 후 크기 조정
          setTimeout(() => {
            if (mapRef.current) {
              mapRef.current.invalidateSize();
            }
          }, 200);
        }}
      >
        {/* 라벨이 잘 보이는 타일 레이어 사용 */}
        <TileLayer
          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/attributions">CARTO</a>'
          url="https://{s}.basemaps.cartocdn.com/rastertiles/voyager_labels_under/{z}/{x}/{y}{r}.png"
          subdomains="abcd"
          minZoom={1}
          maxZoom={20}
        />
        
        <MapBounds countries={mapData.countries} />
        
        {geoData && (
          <GeoJSON
            key={JSON.stringify(mapData)}
            data={geoData}
            style={geoJsonStyle}
            onEachFeature={onEachFeature}
          />
        )}

        {travelRoutes.map((route, index) => (
          <Polyline
            key={`route-${route.userId}`}
            positions={route.path}
            pathOptions={{
              color: route.color,
              weight: 3,
              opacity: 0.7,
              dashArray: '5, 10'
            }}
          />
        ))}

        {clusterData.map((cluster, index) => (
          <Marker
            key={`cluster-${index}`}
            position={cluster.position}
            icon={createClusterIcon(cluster.countries.length, cluster.countries.flatMap(c => c.youtubers))}
            eventHandlers={{
              click: () => {
                if (cluster.countries.length === 1) {
                  onCountryClick(cluster.countries[0].countryCode);
                }
              }
            }}
          />
        ))}
      </MapContainer>

      {/* 범례 토글 버튼 (모바일) */}
      {isMobile && (
        <IconButton
          onClick={() => setShowLegend(!showLegend)}
          sx={{
            position: 'absolute',
            bottom: 20,
            left: 20,
            width: 56,
            height: 56,
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            boxShadow: '0 4px 12px rgba(0,0,0,0.2)',
            zIndex: 10000,
            border: '2px solid rgba(0,0,0,0.1)',
            '&:hover': {
              backgroundColor: 'rgba(255, 255, 255, 1)',
              transform: 'scale(1.05)'
            }
          }}
        >
          <Typography sx={{ fontSize: '24px' }}>
            {showLegend ? '❌' : '📍'}
          </Typography>
        </IconButton>
      )}

      {/* 통계 토글 버튼 (모바일) */}
      {isMobile && (
        <IconButton
          onClick={() => setShowStats(!showStats)}
          sx={{
            position: 'absolute',
            top: 20,
            right: 20,
            width: 56,
            height: 56,
            backgroundColor: 'rgba(255, 255, 255, 0.95)',
            boxShadow: '0 4px 12px rgba(0,0,0,0.2)',
            zIndex: 10000,
            border: '2px solid rgba(0,0,0,0.1)',
            '&:hover': {
              backgroundColor: 'rgba(255, 255, 255, 1)',
              transform: 'scale(1.05)'
            }
          }}
        >
          <Typography sx={{ fontSize: '24px' }}>
            {showStats ? '❌' : '📊'}
          </Typography>
        </IconButton>
      )}

        {/* 개선된 범례 */}
        {(showLegend || !isMobile) && (
          <Box
            sx={{
              position: 'absolute',
              bottom: isMobile ? 90 : 20,
              left: 20,
              backgroundColor: 'rgba(255, 255, 255, 0.98)',
              padding: isMobile ? 2 : 2.5,
              borderRadius: 3,
              boxShadow: '0 8px 24px rgba(0,0,0,0.25)',
              minWidth: isMobile ? 160 : 180,
              maxWidth: isMobile ? 250 : 'none',
              zIndex: 9999,
              border: '2px solid rgba(0,0,0,0.1)',
              backdropFilter: 'blur(8px)',
              transform: isMobile ? (showLegend ? 'translateY(0)' : 'translateY(100%)') : 'none',
              transition: 'transform 0.3s ease',
              maxHeight: isMobile ? '50vh' : 'none',
              overflowY: isMobile ? 'auto' : 'visible'
            }}
          >
            <Typography 
              variant="subtitle2" 
              sx={{ 
                display: 'block', 
                mb: isMobile ? 1.5 : 2, 
                fontWeight: 'bold', 
                color: '#2c3e50',
                fontSize: isMobile ? '13px' : '14px'
              }}
            >
              📍 지도 범례
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: isMobile ? 1 : 1.2 }}>
              <Typography variant="caption" sx={{ fontWeight: 'bold', color: '#34495e', fontSize: isMobile ? '11px' : '12px', mb: 0.5 }}>
                🗺️ 국가별 방문 횟수
              </Typography>
              
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Box sx={{ 
                  width: isMobile ? 14 : 18, 
                  height: isMobile ? 14 : 18, 
                  backgroundColor: '#90caf9', 
                  border: '2px solid #2c3e50', 
                  borderRadius: '3px',
                  boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                }} />
                <Typography variant="caption" sx={{ fontWeight: 'normal', fontSize: isMobile ? '10px' : '12px', color: '#2c3e50' }}>
                  1-4회 방문
                </Typography>
              </Box>
              
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Box sx={{ 
                  width: isMobile ? 14 : 18, 
                  height: isMobile ? 14 : 18, 
                  backgroundColor: '#42a5f5', 
                  border: '2px solid #2c3e50', 
                  borderRadius: '3px',
                  boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                }} />
                <Typography variant="caption" sx={{ fontWeight: 'normal', fontSize: isMobile ? '10px' : '12px', color: '#2c3e50' }}>
                  5-9회 방문
                </Typography>
              </Box>
              
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Box sx={{ 
                  width: isMobile ? 14 : 18, 
                  height: isMobile ? 14 : 18, 
                  backgroundColor: '#1976d2', 
                  border: '2px solid #2c3e50', 
                  borderRadius: '3px',
                  boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                }} />
                <Typography variant="caption" sx={{ fontWeight: 'normal', fontSize: isMobile ? '10px' : '12px', color: '#2c3e50' }}>
                  10-19회 방문
                </Typography>
              </Box>
              
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Box sx={{ 
                  width: isMobile ? 14 : 18, 
                  height: isMobile ? 14 : 18, 
                  backgroundColor: '#0d47a1', 
                  border: '2px solid #2c3e50', 
                  borderRadius: '3px',
                  boxShadow: '0 2px 4px rgba(0,0,0,0.1)'
                }} />
                <Typography variant="caption" sx={{ fontWeight: 'normal', fontSize: isMobile ? '10px' : '12px', color: '#2c3e50' }}>
                  20회 이상 방문
                </Typography>
              </Box>

              <Typography variant="caption" sx={{ fontWeight: 'bold', color: '#34495e', fontSize: isMobile ? '11px' : '12px', mt: 1, mb: 0.5 }}>
                📌 마커 유형
              </Typography>
              
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Box sx={{ 
                  width: isMobile ? 16 : 20, 
                  height: isMobile ? 16 : 20, 
                  backgroundColor: '#ff4444', 
                  border: '2px solid white', 
                  borderRadius: '50%',
                  boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
                }} />
                <Typography variant="caption" sx={{ fontWeight: 'normal', fontSize: isMobile ? '10px' : '12px', color: '#2c3e50' }}>
                  단일 국가/도시
                </Typography>
              </Box>
              
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Box sx={{ 
                  width: isMobile ? 18 : 22, 
                  height: isMobile ? 18 : 22, 
                  backgroundColor: '#ff8800', 
                  border: '2px solid white', 
                  borderRadius: '50%',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: isMobile ? '8px' : '10px',
                  fontWeight: 'bold',
                  color: 'white',
                  boxShadow: '0 2px 4px rgba(0,0,0,0.2)'
                }}>
                  2+
                </Box>
                <Typography variant="caption" sx={{ fontWeight: 'normal', fontSize: isMobile ? '10px' : '12px', color: '#2c3e50' }}>
                  다중 방문 지역
                </Typography>
              </Box>

              <Typography variant="caption" sx={{ fontWeight: 'bold', color: '#34495e', fontSize: isMobile ? '11px' : '12px', mt: 1, mb: 0.5 }}>
                🛣️ 여행 경로
              </Typography>
              
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <Box sx={{ 
                  width: isMobile ? 20 : 24, 
                  height: 3, 
                  background: 'linear-gradient(90deg, #ff6b6b, #4ecdc4, #45b7d1, #f9ca24)', 
                  borderRadius: '2px',
                  border: '1px solid #2c3e50'
                }} />
                <Typography variant="caption" sx={{ fontWeight: 'normal', fontSize: isMobile ? '10px' : '12px', color: '#2c3e50' }}>
                  여행 경로
                </Typography>
              </Box>
            </Box>
          </Box>
        )}

        {/* 통계 정보 */}
        {(showStats || !isMobile) && (
          <Box
            sx={{
              position: 'absolute',
              top: isMobile ? 90 : 16,
              right: isMobile ? 20 : 16,
              backgroundColor: 'rgba(255, 255, 255, 0.98)',
              padding: isMobile ? 1.5 : 2,
              borderRadius: 2,
              boxShadow: '0 4px 12px rgba(0,0,0,0.2)',
              minWidth: isMobile ? 140 : 150,
              maxWidth: isMobile ? 200 : 'none',
              zIndex: 1001,
              border: '1px solid rgba(0,0,0,0.1)',
              backdropFilter: 'blur(8px)',
              transform: isMobile ? (showStats ? 'translateY(0)' : 'translateY(-100%)') : 'none',
              transition: 'transform 0.3s ease'
            }}
          >
            <Typography variant="caption" sx={{ display: 'block', mb: isMobile ? 1 : 1.5, fontWeight: 'bold', color: '#333', fontSize: isMobile ? '12px' : '13px' }}>
              📊 전체 통계
            </Typography>
            <Box sx={{ display: 'flex', flexDirection: 'column', gap: isMobile ? 0.5 : 0.8 }}>
              <Typography variant="body2" sx={{ fontWeight: 'normal', fontSize: isMobile ? '11px' : '13px' }}>
                방문 국가: <strong style={{ color: '#1976d2' }}>{mapData.countries.length}개</strong>
              </Typography>
              <Typography variant="body2" sx={{ fontWeight: 'normal', fontSize: isMobile ? '11px' : '13px' }}>
                총 방문: <strong style={{ color: '#1976d2' }}>{mapData.countries.reduce((sum, c) => sum + c.visitCount, 0)}회</strong>
              </Typography>
              <Typography variant="body2" sx={{ fontWeight: 'normal', fontSize: isMobile ? '11px' : '13px' }}>
                여행 경로: <strong style={{ color: '#1976d2' }}>{travelRoutes.length}개</strong>
              </Typography>
            </Box>
          </Box>
        )}
    </Box>
  );
};

export default WorldMap; 