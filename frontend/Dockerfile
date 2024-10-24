FROM node:20.9.0-bullseye-slim as build

WORKDIR /app

COPY package*.json ./
RUN npm config set registry https://registry.npmjs.org/
RUN npm ci --verbose 

EXPOSE 3000

COPY . ./

CMD ["npm", "start"]
