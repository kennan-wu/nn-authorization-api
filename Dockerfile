FROM openjdk:23-jdk-slim

WORKDIR /authServerApp

COPY target/mp3-player-api-0.0.1-SNAPSHOT.jar authServerApp.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "authServerApp.jar"]
