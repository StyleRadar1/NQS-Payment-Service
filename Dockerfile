# Etapa 1: build reproducible
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# Etapa 2: runtime mínimo
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S styleradar && adduser -S styleradar -G styleradar

COPY --from=build /app/target/*.jar app.jar
COPY certs/ /app/certs/

USER styleradar
EXPOSE 8400

ENTRYPOINT ["java", "-jar", "app.jar"]
