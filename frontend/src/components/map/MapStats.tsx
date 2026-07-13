import React from 'react';
import { Box, Typography, IconButton } from '@mui/material';
import { MapData } from '../../types';

interface MapStatsProps {
  mapData: MapData;
  travelRoutesCount: number;
  isMobile: boolean;
  show: boolean;
  onToggle: () => void;
}

// 우상단 통계 패널: 방문 국가 수 / 총 방문 횟수 / 여행 경로 수. 모바일에서만 토글 버튼으로 열고 닫는다.
const MapStats: React.FC<MapStatsProps> = ({ mapData, travelRoutesCount, isMobile, show, onToggle }) => {
  return (
    <>
      {/* 통계 토글 버튼 (모바일) */}
      {isMobile && (
        <IconButton
          onClick={onToggle}
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
            {show ? '❌' : '📊'}
          </Typography>
        </IconButton>
      )}

      {(show || !isMobile) && (
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
            transform: isMobile ? (show ? 'translateY(0)' : 'translateY(-100%)') : 'none',
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
              여행 경로: <strong style={{ color: '#1976d2' }}>{travelRoutesCount}개</strong>
            </Typography>
          </Box>
        </Box>
      )}
    </>
  );
};

export default MapStats;
