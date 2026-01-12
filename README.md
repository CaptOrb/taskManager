# Task Manager

## Tech Stack
- Backend: Java, Spring Boot

- Frontend: React, TypeScript, TailwindCSS

- Deployment: AWS (CloudFormation, S3, ECS); CI/CD via GitHub Actions (tests, ESLint, deployment)

- Database: MySQL, Flyway (for DB migrations)

- API documentation: Swagger

- Testing tools: JUnit, Mockito

## About

Task Manager where users can create, update, delete, and view their own tasks. Features include Markdown support, task status tracking, and filtering by status, due date, and priority. Authentication is handled using JWT.

App was previously deployed to AWS using CloudFormation and a CI/CD pipeline, now deployed to Oracle Cloud due to ending of AWS free tier.

See the deployed website at https://taskapp.conordev.com

## Prerequisites
* Docker

## Compiling and running code
1. Clone the repository:

   ```sh
   git clone git@github.com:CaptOrb/taskManager.git
   ```
2. Rename `.env.sample` to `.env` and fill in the database credentials and JWT secret in that file.
3. Inside the project's root directory, run:

   ```sh
   docker compose up --build
   ```
4. Frontend will be available at http://localhost:3000

