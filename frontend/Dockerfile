FROM node:18-alpine AS builder
WORKDIR /app
COPY . .
RUN npm install && npm run build

FROM nginx:alpine
# 줄바꿈 형식 오류를 수정하기 위해 dos2unix를 설치하고 실행합
RUN apk add --no-cache dos2unix && \
    find / -name "docker-entrypoint.sh" -exec dos2unix {} \;

COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80

