FROM node:22.18-bookworm-slim AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci --verbose

COPY . ./

RUN npm run build

FROM node:22.18-bookworm-slim AS development

WORKDIR /app

COPY package*.json ./

RUN npm install

COPY . ./

EXPOSE 3000
CMD ["npm", "run", "dev"]


FROM caddy:2-alpine AS prod

COPY --from=build /app/dist /usr/share/caddy
COPY Caddyfile /etc/caddy/Caddyfile
