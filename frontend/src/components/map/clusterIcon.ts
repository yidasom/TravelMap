import L from 'leaflet';

// 클러스터 마커 아이콘 생성 (묶인 국가들의 총 방문 횟수에 따라 크기/색상 결정)
export const createClusterIcon = (count: number): L.DivIcon => {
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
    iconAnchor: [size / 2, size / 2]
  });
};
