# acm-website-backend

Backend of the official website of PEC ACM CSS

## Pre Requisites

1. Java 17(Oracle or Corretto) installed.
2. Intellij IDEA
3. Docker

## Setup

1. Configure the springboot project in Intellij and Activate `local` profile.
2. Run the `data.sql` file in resources to load the seed data.
3. Run code in Intellij
4. Code, Code, Code, Review, Code, Code, Review, Push, PR, Merge

> You need Intellij IDEA Ultimate, avail it by student discount

### For Frontend Development (Docker)

1. Create `.env` file:
   ```bash
   cp .env.example .env
   ```
   Edit `.env` with your credentials (especially POSTGRES_PASSWORD and SMTP credentials)

2. Start the backend:
   ```bash
   docker-compose up -d
   ```

3. Backend is now running at: `http://localhost:8080`

4. When done, stop the backend:
   ```bash
   docker-compose stop
   ```

> [!WARNING]
> Use `docker-compose stop` to pause services. Only use `docker-compose down` if you want to completely remove containers and networks. Use `docker-compose down -v` to also remove database data (full reset).

## Instructions

- Always work in separate branch for each feature.
- Do not create branches with your name like `xyz`, rather create branches based on branch or issue name,
  like `feat-user-service`.
- Take pull of latest `main` branch before pushing code.

Goodluck :)

## Contributors

<a href="https://github.com/PEC-CSS/acm-website-backend/graphs/contributors">
  <img alt="contributors" src="https://contrib.rocks/image?repo=PEC-CSS/acm-website-backend" />
</a>
