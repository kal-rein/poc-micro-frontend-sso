FROM node:20.11.1-alpine AS build

WORKDIR /usr/src/app

COPY package.json package-lock.json ./
RUN npm install

COPY . .
RUN npm run build:prod

FROM nginx:1.24.0-alpine

COPY container/nginx.conf /etc/nginx/nginx.conf
COPY --from=build /usr/src/app/dist/host/browser /usr/share/nginx/html

EXPOSE 80
