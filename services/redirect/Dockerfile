# syntax=docker/dockerfile:1.6

FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Minimal tools for HEALTHCHECK + create non-root user
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/* && \
    groupadd -r app && useradd -r -g app app

COPY --from=build /workspace/target/*.jar /app/app.jar

USER app

ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8081 \
    JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

EXPOSE 8081

HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=5 \
  CMD curl -fsS http://127.0.0.1:${SERVER_PORT}/actuator/health || exit 1

ENTRYPOINT ["sh","-lc","exec java $JAVA_OPTS -Dserver.port=${SERVER_PORT} -jar /app/app.jar"]