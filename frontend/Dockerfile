FROM node:18-alpine AS builder
WORKDIR /app
COPY . .
RUN npm install && npm run build

# nginx
FROM nginx:alpine
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]