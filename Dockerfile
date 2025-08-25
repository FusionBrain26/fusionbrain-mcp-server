FROM openjdk:17-alpine

ENV JAVA_OPTS=""

WORKDIR /app
COPY ./target/*.jar app.jar

ENTRYPOINT exec java $JAVA_OPTS -jar app.jar
