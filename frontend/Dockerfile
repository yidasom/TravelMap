FROM --platform=linux/amd64 node:18-alpine AS builder
WORKDIR /app
COPY . .
# dos2unix를 npm install 전에 설치합니다.
RUN apk add --no-cache dos2unix
RUN npm install && npm run build

FROM --platform=linux/amd64 nginx:alpine

COPY --from=builder /app/build /usr/share/nginx/html
COPY docker-entrypoint.sh /docker-entrypoint.sh

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]