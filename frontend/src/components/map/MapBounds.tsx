import React, { useEffect } from 'react';
import { useMap } from 'react-leaflet';
import L from 'leaflet';
import { CountryData } from '../../types';
import { getCountryCoordinates } from './countryCoordinates';

// 지도 바운딩 및 반응형 처리 컴포넌트
const MapBounds: React.FC<{ countries: CountryData[] }> = ({ countries }) => {
  const map = useMap();

  useEffect(() => {
    // 드래그 가능 범위는 지도 전체로 두고(전세계를 넘어가는 무한 반복만 방지),
    // 방문 국가 데이터는 초기 화면 위치를 잡는 용도로만 사용한다.
    // (예전엔 방문 국가 주변으로 maxBounds를 세게 좁혀서 마우스로 자유롭게 돌려보기 어려웠음)
    const worldBounds = L.latLngBounds(
      L.latLng(-85, -180),
      L.latLng(85, 180)
    );
    map.setMaxBounds(worldBounds);

    if (countries.length > 0) {
      const bounds = countries.map(country => {
        const coords = getCountryCoordinates(country.countryCode);
        return L.latLng(coords[0], coords[1]);
      });

      if (bounds.length > 0) {
        const group = L.featureGroup(bounds.map(coord => L.marker(coord)));
        const boundsObj = group.getBounds();

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
      map.setView(getCountryCoordinates('KR'), 7);
    }
  }, [countries, map]);

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

export default MapBounds;
