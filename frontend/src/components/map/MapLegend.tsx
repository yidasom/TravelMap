import React from 'react';
import { Box, Typography, IconButton } from '@mui/material';

interface MapLegendProps {
  isMobile: boolean;
  show: boolean;
  onOpen: () => void;
  onClose: () => void;
}

// 지도 범례: 국가별 방문 횟수 색상, 마커 유형, 여행 경로 설명.
// 모바일/데스크탑 모두 접고 펼 수 있으며, 닫혀 있을 땐 좌하단에 열기 버튼만 표시한다.
const MapLegend: React.FC<MapLegendProps> = ({ isMobile, show, onOpen, onClose }) => {
  return (
    <>
      {/* 범례 열기 버튼 (범례가 닫혀 있을 때만 표시, 모바일/데스크탑 공통) */}
      {!show && (
        <IconButton
          onClick={onOpen}
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
          <Typography sx={{ fontSize: '24px' }}>📍</Typography>
        </IconButton>
      )}

      {show && (
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
            maxHeight: isMobile ? '50vh' : 'none',
            overflowY: isMobile ? 'auto' : 'visible'
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', mb: isMobile ? 1.5 : 2 }}>
            <Typography
              variant="subtitle2"
              sx={{
                fontWeight: 'bold',
                color: '#2c3e50',
                fontSize: isMobile ? '13px' : '14px'
              }}
            >
              📍 지도 범례
            </Typography>
            <IconButton
              size="small"
              onClick={onClose}
              sx={{ p: 0.5, ml: 1 }}
              aria-label="범례 접기"
            >
              <Typography sx={{ fontSize: '14px' }}>✕</Typography>
            </IconButton>
          </Box>
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
                1회 방문
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
                2-3
              </Box>
              <Typography variant="caption" sx={{ fontWeight: 'normal', fontSize: isMobile ? '10px' : '12px', color: '#2c3e50' }}>
                2-3회 방문
              </Typography>
            </Box>

            <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
              <Box sx={{
                width: isMobile ? 20 : 24,
                height: isMobile ? 20 : 24,
                backgroundColor: '#ff0000',
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
                4+
              </Box>
              <Typography variant="caption" sx={{ fontWeight: 'normal', fontSize: isMobile ? '10px' : '12px', color: '#2c3e50' }}>
                4회 이상 방문
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
    </>
  );
};

export default MapLegend;
