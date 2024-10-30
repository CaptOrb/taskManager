# Task Manager

## About

Task Manager which has a Spring Boot REST API back-end and React front-end. 
Users can create, update, delete, and view their own tasks with task status tracking and filter by task status, due date and priority.

## Prerequisites
* Docker

# Compiling and running code
1. Clone the repository. 
   ```sh
   git clone git@github.com:CaptOrb/taskManager.git
   ```
2. Rename .env.sample to .env and fill in the database credentials and JWT secret in that file.
2. Inside the project's root directory run ```docker compose up --build```
3. Frontend will be available at http://localhost:3000

