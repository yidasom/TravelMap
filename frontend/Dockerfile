FROM node:18-alpine AS builder
WORKDIR /app

# package.json / package-lock.json만 먼저 복사 → 캐시 활용
COPY package*.json ./

# 의존성 설치
RUN npm ci

# 전체 소스 복사
COPY . .

# Node 메모리 힙 늘리기
ARG NODE_OPTIONS="--max-old-space-size=4096"
ENV NODE_OPTIONS=$NODE_OPTIONS

# 빌드
RUN npm run build

# 2. nginx entrypoint 추출
FROM nginx:alpine AS nginx_stage

FROM nginx:alpine
COPY --from=nginx_stage /docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
