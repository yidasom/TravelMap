# FROM --platform=linux/amd64 node:18-alpine AS build
FROM --platform=linux/arm64 node:18-alpine AS build
WORKDIR /app
COPY package.json ./
COPY package-lock.json ./
RUN npm install
COPY . ./
RUN npm run build

# FROM --platform=linux/amd64 nginx:1.21-alpine
FROM --platform=linux/arm64 nginx:1.21-alpine
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]