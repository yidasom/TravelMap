FROM --platform=linux/amd64 node:18-alpine AS builder
WORKDIR /app
COPY . .
RUN apk add --no-cache dos2unix
RUN npm install && npm run build

FROM --platform=linux/amd64 alpine AS dos2unix-stage
RUN apk update && apk add dos2unix

WORKDIR /app
COPY ../docker-entrypoint.sh .
RUN dos2unix docker-entrypoint.sh

FROM --platform=linux/amd64 nginx:alpine

COPY --from=dos2unix-stage /app/docker-entrypoint.sh /docker-entrypoint.sh

COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]