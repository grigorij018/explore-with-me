FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY . .
RUN mvn -B -pl ewm-stats-service/stats-server -am package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /workspace/ewm-stats-service/stats-server/target/stats-server-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
