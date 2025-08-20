FROM node:18-alpine AS builder
WORKDIR /app
COPY . .
RUN npm install && npm run build

FROM alpine AS dos2unix-stage
RUN apk update && apk add dos2unix

# Copy docker-entrypoint.sh to the dos2unix-stage and fix CRLF issues
WORKDIR /app
COPY ../docker-entrypoint.sh .
RUN dos2unix docker-entrypoint.sh

FROM nginx:alpine

COPY --from=dos2unix-stage /app/docker-entrypoint.sh /docker-entrypoint.sh

COPY --from=builder /app/build /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]