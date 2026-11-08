# Happy Tails

Web system for adopting cats and dogs from a shelter.

## Tech stack

React + Vite + Tailwind, Spring Boot (Java 21, Maven), PostgreSQL, Docker Compose.

## Requirements

Java 21, Maven, Node 20+, Docker Desktop.

## Running for development

1. Set up environment variables:

        cp .env.example .env

2. Start the database:

        docker compose up -d

3. Start the backend (http://localhost:8080):

        cd backend
        mvn spring-boot:run

4. Start the frontend (http://localhost:5173):

        cd frontend
        npm install
        npm run dev

## Stopping

Database: `docker compose down`. Data is kept in a Docker volume. To delete it as well: `docker compose down -v`.

## Environment variables

| Name | Purpose | Required |
|---|---|---|
| POSTGRES_DB | database name | no (happytails) |
| POSTGRES_USER | database user | no (happytails) |
| POSTGRES_PASSWORD | database password | yes, set in .env |
| POSTGRES_PORT | database port | no (5432) |

The `.env` file is not committed to Git. Use `.env.example` as a template.
