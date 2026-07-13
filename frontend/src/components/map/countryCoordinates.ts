// 국가 좌표 매핑
export const countryCoordinates: { [key: string]: [number, number] } = {
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
export const getCountryCoordinates = (countryCode: string): [number, number] => {
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
