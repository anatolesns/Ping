# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Cache des dependances : on copie d'abord le pom seul
COPY pom.xml .
RUN mvn -q dependency:go-offline

# Puis le code et on package en fast-jar
COPY src ./src
RUN mvn -q clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre
WORKDIR /app

# Repertoire requis
RUN mkdir -p /tmp/ping

# Copie des artefacts fast-jar
COPY --from=build /build/target/quarkus-app/lib/ ./lib/
COPY --from=build /build/target/quarkus-app/*.jar ./
COPY --from=build /build/target/quarkus-app/app/ ./app/
COPY --from=build /build/target/quarkus-app/quarkus/ ./quarkus/

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]