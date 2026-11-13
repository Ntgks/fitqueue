FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn --batch-mode -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/fitqueue-0.0.1-SNAPSHOT.jar app.jar

USER 10001:10001
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]