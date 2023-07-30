# acm-website-backend
Backend of the official website of PEC ACM CSS

## Pre Requisites

1. Java 17(Oracle or Corretto) installed.
2. Intellij IDEA
3. Docker

## Setup
1. Configure the project in Intellij and Activate `local` profile.
2. Run the following command in terminal to activate and connect to local database.
    ```bash
    docker compose up
    ```
3. Add the Email/password of gmail in `application-local.yml` file under `spring.mail` key, to connect to smtp server and send mails.
   - DO NOT PUSH THIS FILE 
4. Code and push

## Instructions

- Always work in separate branch for each feature.
- Do not create branches with your name like `xyz`, rather create branches based on branch or issue name, like `feat-user-service`.
- Take pull of latest `main` branch before pushing code.

Goodluck :)

## Contributors
<a href="https://github.com/PEC-CSS/acm-website-backend/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=PEC-CSS/acm-website-backend" />
</a>
