# Etapa 1: Build
FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
# Descarga dependencias para aprovechar la caché de Docker
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Etapa 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Copia solo el JAR construido
COPY --from=builder /app/target/*.jar app.jar
# Expone el puerto definido en la matriz (8090)
EXPOSE 8090
# Comando para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]