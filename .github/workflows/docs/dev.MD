# Task Manager Development

## Prerequisites
* Docker
* Java 21
* Maven
* NPM

## Running in dev mode


1. Rename `.env.sample` to `.env` and fill in the required keys in that file.
2. Inside the project's root directory, run:

   ```sh
   docker compose up -f docker-compose-dev.yml
   ```
3. Inside /backend, run:

   ```
   mvn spring-boot:run
   ```

4. Inside /frontend, run:

   ```
   npm run dev
   ```
5. Frontend will be available at http://localhost:3000

## Running tests
1. Inside the project's root directory, run:

   ```sh
   docker compose up -f docker-compose-dev.yml
   ```

2. Inside /backend run
    ```
    mvn test
    ```