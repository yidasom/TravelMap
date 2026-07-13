import React, { useEffect, useState, useRef } from 'react';
import { MapContainer, TileLayer, GeoJSON, Polyline } from 'react-leaflet';
import { Box, Typography, CircularProgress, Backdrop } from '@mui/material';
import { MapData, CountryData } from '../types';
import 'leaflet/dist/leaflet.css';
import MapBounds from './map/MapBounds';
import ClusterMarkers from './map/ClusterMarkers';
import MapLegend from './map/MapLegend';
import MapStats from './map/MapStats';
import { getCountryCoordinates } from './map/countryCoordinates';

interface WorldMapProps {
  mapData: MapData;
  onCountryClick: (countryCode: string) => void;
}

// 세계 지도 GeoJSON URL
// 기존에 쓰던 D3-graph-gallery 데이터셋은 ISO_A2 속성이 없고 177개국만 포함해 싱가포르 같은
// 도시국가가 아예 빠져 있었다. Natural Earth 50m 데이터셋은 ISO_A2를 직접 제공하고 싱가포르도 포함한다.
const geoUrl = "https://raw.githubusercontent.com/nvkelso/natural-earth-vector/master/geojson/ne_50m_admin_0_countries.geojson";

const WorldMap: React.FC<WorldMapProps> = ({ mapData, onCountryClick }) => {
  const [geoData, setGeoData] = useState<any>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [mapReady, setMapReady] = useState(false);
  const [showLegend, setShowLegend] = useState(false);
  const [showStats, setShowStats] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 768);
  const mapRef = useRef<any>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  // 국가별 데이터를 코드로 매핑 (Natural Earth geojson의 ISO_A2 속성과 매칭)
  const countryDataMap = new Map<string, CountryData>();
  mapData.countries.forEach(country => {
    countryDataMap.set(country.countryCode.toUpperCase(), country);
  });

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
  const getCountryColor = (countryData: CountryData | undefined): string => {
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
    const countryData = countryDataMap.get(feature.properties.ISO_A2);

    return {
      fillColor: getCountryColor(countryData),
      weight: countryData ? 2 : 0.5,
      opacity: 1,
      color: countryData ? '#2c3e50' : '#bdc3c7',
      fillOpacity: countryData ? 0.85 : 0.2,
      dashArray: countryData ? '' : '3,3',
    };
  };

  // 국가 클릭 이벤트 핸들러
  const onEachFeature = (feature: any, layer: any) => {
    const countryData = countryDataMap.get(feature.properties.ISO_A2);

    if (countryData) {
      layer.bindTooltip(
        `${countryData.countryEmoji ?? ''} ${countryData.countryName} — ${countryData.visitCount}회 방문`,
        { sticky: true }
      );
    }

    layer.on({
      click: (e: any) => {
        if (countryData) {
          onCountryClick(countryData.countryCode);
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

        {travelRoutes.map((route) => (
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

        <ClusterMarkers mapData={mapData} onCountryClick={onCountryClick} />
      </MapContainer>

      <MapLegend
        isMobile={isMobile}
        show={showLegend}
        onOpen={() => setShowLegend(true)}
        onClose={() => setShowLegend(false)}
      />

      <MapStats
        mapData={mapData}
        travelRoutesCount={travelRoutes.length}
        isMobile={isMobile}
        show={showStats}
        onToggle={() => setShowStats(!showStats)}
      />
    </Box>
  );
};

export default WorldMap;
