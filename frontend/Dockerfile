FROM --platform=linux/arm64 node:18-alpine AS builder
WORKDIR /app
COPY . .
RUN npm install && npm run build

FROM --platform=linux/arm64 nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
