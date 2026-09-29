# ── Stage 1: Build ────────────────────────────────────────────────────
FROM maven:3.9.8-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (layer-cached separately from source)
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# ── Stage 2: Runtime ──────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/novis-backend-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/uploads/kyc
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
