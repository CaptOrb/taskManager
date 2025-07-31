FROM node:20.18.0-bullseye-slim AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci --verbose

COPY . ./

RUN npm run build

FROM node:20.18.0-bullseye-slim AS development

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . ./

EXPOSE 3000
CMD ["npm", "run", "dev"]


FROM nginx:1.25.1-alpine AS prod

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx-default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]