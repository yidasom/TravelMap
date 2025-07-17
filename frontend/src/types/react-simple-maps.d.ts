declare module 'react-simple-maps' {
  import { ReactNode, CSSProperties } from 'react';

  interface GeographyProps {
    geography: any;
    key?: string;
    fill?: string;
    stroke?: string;
    strokeWidth?: number;
    onClick?: (geo: any) => void;
    style?: {
      default?: CSSProperties;
      hover?: CSSProperties;
      pressed?: CSSProperties;
    };
    children?: ReactNode;
  }

  interface ComposableMapProps {
    projection?: string;
    projectionConfig?: {
      scale?: number;
      center?: [number, number];
      rotate?: [number, number, number];
    };
    style?: CSSProperties;
    width?: number;
    height?: number;
    children?: ReactNode;
  }

  interface GeographiesProps {
    geography: string;
    children: (props: { geographies: any[] }) => ReactNode;
  }

  interface TooltipProps {
    content: ReactNode;
  }

  export const ComposableMap: React.FC<ComposableMapProps>;
  export const Geographies: React.FC<GeographiesProps>;
  export const Geography: React.FC<GeographyProps>;
  export const Tooltip: React.FC<TooltipProps>;
} 