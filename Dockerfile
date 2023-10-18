FROM openjdk:latest
VOLUME /tmp
EXPOSE 8080

LABEL maintainer="PECACM acmcss@pec.edu.in"
LABEL version="1.0"
LABEL description="Backend API for the official website of acmcss"

ARG JAR_FILE=target/backend-1.0.1.jar

ADD ${JAR_FILE} app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
