import React from 'react';
import { Marker } from 'react-leaflet';
import { MapData, CountryData } from '../../types';
import { getCountryCoordinates } from './countryCoordinates';
import { createClusterIcon } from './clusterIcon';

interface ClusterMarkersProps {
  mapData: MapData;
  onCountryClick: (countryCode: string) => void;
}

interface Cluster {
  position: [number, number];
  countries: CountryData[];
  totalVisits: number;
}

// 좌표가 같은(가까운) 국가들을 하나의 원형 마커로 묶어서 표시.
// 마커 크기/색상은 묶인 국가들의 총 방문 횟수(totalVisits) 기준이며, 클릭 시 국가가 하나뿐이면 바로 필터링한다.
const ClusterMarkers: React.FC<ClusterMarkersProps> = ({ mapData, onCountryClick }) => {
  const clusterData = React.useMemo(() => {
    const clusters = new Map<string, Cluster>();

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

  return (
    <>
      {clusterData.map((cluster, index) => (
        <Marker
          key={`cluster-${index}`}
          position={cluster.position}
          icon={createClusterIcon(cluster.totalVisits)}
          eventHandlers={{
            click: () => {
              if (cluster.countries.length === 1) {
                onCountryClick(cluster.countries[0].countryCode);
              }
            }
          }}
        />
      ))}
    </>
  );
};

export default ClusterMarkers;
