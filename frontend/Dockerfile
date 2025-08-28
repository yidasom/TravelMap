FROM --platform=linux/arm64 node:18-alpine AS builder
WORKDIR /app
COPY . .
RUN npm config set registry https://registry.npmjs.org/ \
  && npm install --fetch-retries=5 --fetch-retry-factor=2 \
  --fetch-retry-mintimeout=10000 --fetch-retry-maxtimeout=60000 \
  && npm run build

FROM --platform=linux/arm64 nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
