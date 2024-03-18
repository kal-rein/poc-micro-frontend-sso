FROM node:20.11.1-alpine AS build

ARG backNumber

WORKDIR /usr/src/app

COPY package.json package-lock.json ./
RUN npm install

COPY . .
RUN npm run build:back-$backNumber

FROM nginx:1.24.0-alpine

COPY container/nginx.conf /etc/nginx/nginx.conf
COPY --from=build /usr/src/app/dist/app/browser /usr/share/nginx/html

EXPOSE 80
