# Image de base avec Java 17
FROM openjdk:17-jdk-alpine

LABEL maintainer="ranim.ahmadi"
LABEL project="projet-foyer-crud"
LABEL repository="https://github.com/ranim-ahmadi/Projet-foyer-crud.git"
LABEL version="1.0"

WORKDIR /app

COPY target/*.jar tp-foyer-app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring


EXPOSE 8089

# Health check pour Docker
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8089/tpfoyer/actuator/health || exit 1

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xms256m -Xmx512m"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar tp-foyer-app.jar