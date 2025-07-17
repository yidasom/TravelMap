import React from 'react';
import {
  ComposableMap,
  Geographies,
  Geography
} from 'react-simple-maps';
import { Box, Typography, useTheme } from '@mui/material';
import { MapData, CountryData } from '../types';
import { Tooltip, TooltipProvider } from "react-tooltip"; 


interface WorldMapProps {
  mapData: MapData;
  onCountryClick: (countryCode: string) => void;
}

// 세계 지도 토폴로지 URL (Natural Earth 데이터)
const geoUrl = "https://cdn.jsdelivr.net/npm/world-atlas@2/countries-110m.json";

const WorldMap: React.FC<WorldMapProps> = ({ mapData, onCountryClick }) => {
  const theme = useTheme();

  // 국가별 데이터를 코드로 매핑
  const countryDataMap = new Map<string, CountryData>();
  mapData.countries.forEach(country => {
    countryDataMap.set(country.countryCode, country);
  });

  // 방문 횟수에 따른 색상 계산
  const getCountryColor = (countryCode: string): string => {
    const countryData = countryDataMap.get(countryCode);
    if (!countryData) {
      return '#f0f0f0'; // 방문하지 않은 국가
    }

    const visitCount = countryData.visitCount;
    
    // 방문 횟수에 따른 색상 강도 (1-10회: 연한 파란색, 11회 이상: 진한 파란색)
    if (visitCount >= 20) return '#0d47a1'; // 매우 진한 파란색
    if (visitCount >= 10) return '#1976d2'; // 진한 파란색
    if (visitCount >= 5) return '#42a5f5';  // 중간 파란색
    if (visitCount >= 1) return '#90caf9';  // 연한 파란색
    
    return '#f0f0f0';
  };

  // 국가 클릭 핸들러
  const handleCountryClick = (geo: any) => {
    const countryCode = geo.properties.ISO_A2;
    if (countryCode && countryDataMap.has(countryCode)) {
      onCountryClick(countryCode);
    }
  };

  // 툴팁 내용 생성
  const getTooltipContent = (geo: any): React.ReactNode => {
    const countryCode = geo.properties.ISO_A2;
    const countryName = geo.properties.NAME;
    const countryData = countryDataMap.get(countryCode);

    if (!countryData) {
      return (
        <Box sx={{ padding: 1 }}>
          <Typography variant="body2">{countryName}</Typography>
          <Typography variant="caption">방문 기록 없음</Typography>
        </Box>
      );
    }

    return (
      <Box sx={{ padding: 1, maxWidth: 250 }}>
        <Typography variant="subtitle2" fontWeight="bold">
          {countryData.countryEmoji} {countryData.countryName}
        </Typography>
        <Typography variant="body2">
          방문 횟수: {countryData.visitCount}회
        </Typography>
        <Typography variant="body2">
          유튜버 수: {countryData.youtuberCount}명
        </Typography>
        {countryData.youtubers.length > 0 && (
          <Typography variant="caption" sx={{ display: 'block', mt: 1 }}>
            {countryData.youtubers.slice(0, 3).map(y => y.name).join(', ')}
            {countryData.youtubers.length > 3 && ` 외 ${countryData.youtubers.length - 3}명`}
          </Typography>
        )}
      </Box>
    );
  };

  return (
    <Box sx={{ width: '100%', height: '500px', position: 'relative' }}>
      <ComposableMap
        projection="geoNaturalEarth1"
        projectionConfig={{
          scale: 140,
        }}
        style={{
          width: '100%',
          height: '100%',
        }}
      >
        <Geographies geography={geoUrl}>
          {({ geographies }) =>
            geographies.map((geo) => {
              const countryCode = geo.properties.ISO_A2;
              const hasData = countryDataMap.has(countryCode);
              
              return (
                <Geography
                  key={geo.rsmKey}
                  geography={geo}
                  fill={getCountryColor(countryCode)}
                  stroke="#ffffff"
                  data-tooltip-id="my-tooltip"
                  data-tooltip-content={geo.properties.name} // Display country name
                  strokeWidth={0.5}
                  onClick={() => handleCountryClick(geo)}
                  style={{
                    default: {
                      outline: 'none',
                      cursor: hasData ? 'pointer' : 'default',
                    },
                    hover: {
                      fill: hasData ? theme.palette.primary.dark : '#f0f0f0',
                      outline: 'none',
                      cursor: hasData ? 'pointer' : 'default',
                    },
                    pressed: {
                      fill: hasData ? theme.palette.primary.main : '#f0f0f0',
                      outline: 'none',
                    },
                  }}
                >
                  <Tooltip id="my-tooltip" />
                  {/* <ReactTooltip
                    id="map-tooltip"
                    place="top"
                    float
                    style={{ whiteSpace: 'pre-line' }}
                  /> */}
                </Geography>
              );
            })
          }
        </Geographies>
      </ComposableMap>
      
      {/* 범례 */}
      <Box
        sx={{
          position: 'absolute',
          bottom: 16,
          left: 16,
          backgroundColor: 'rgba(255, 255, 255, 0.9)',
          padding: 2,
          borderRadius: 1,
          boxShadow: 1,
        }}
      >
        <Typography variant="caption" fontWeight="bold" sx={{ display: 'block', mb: 1 }}>
          방문 횟수
        </Typography>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 0.5 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box sx={{ width: 16, height: 16, backgroundColor: '#90caf9' }} />
            <Typography variant="caption">1-4회</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box sx={{ width: 16, height: 16, backgroundColor: '#42a5f5' }} />
            <Typography variant="caption">5-9회</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box sx={{ width: 16, height: 16, backgroundColor: '#1976d2' }} />
            <Typography variant="caption">10-19회</Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Box sx={{ width: 16, height: 16, backgroundColor: '#0d47a1' }} />
            <Typography variant="caption">20회 이상</Typography>
          </Box>
        </Box>
      </Box>
    </Box>
  );
};

export default WorldMap; 