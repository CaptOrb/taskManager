# Task Manager

## About

Task Manager which has a Spring Boot REST API back-end and React front-end. 
Users can create, update, delete, and view their own tasks with task status tracking and filter by task status, due date and priority.

App was previously deployed to AWS using CloudFormation and a CI/CD pipeline, now deployed to Oracle Cloud due to ending of AWS free tier.

See the deployed website at https://taskapp.conordev.com

## Prerequisites
* Docker

# Compiling and running code
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

