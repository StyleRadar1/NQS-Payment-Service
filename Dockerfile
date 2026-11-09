# Etapa 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app

# Copia solo el pom.xml para cachear dependencias
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia el código fuente y construye
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8400
ENTRYPOINT ["java", "-jar", "app.jar"]