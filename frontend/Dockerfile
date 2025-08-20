FROM node:18-alpine AS builder
WORKDIR /app
COPY . .
RUN npm install && npm run build

FROM --platform=linux/amd64 nginx:alpine

RUN apk update && apk install -y dos2unix
COPY --chown=nginx:nginx ../docker-entrypoint.sh /docker-entrypoint.sh
RUN dos2unix /docker-entrypoint.sh

COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]