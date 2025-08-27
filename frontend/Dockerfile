FROM --platform=linux/amd64 node:18-alpine AS builder
WORKDIR /app
COPY . .
RUN npm install && npm run build

# nginx entrypoint 추출
FROM --platform=linux/amd64 nginx:alpine AS nginx_stage

FROM --platform=linux/amd64 nginx:alpine
COPY --from=nginx_stage /docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80
ENTRYPOINT ["/docker-entrypoint.sh"]
CMD ["nginx", "-g", "daemon off;"]
