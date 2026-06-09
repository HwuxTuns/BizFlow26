# Build Report Service
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src src
RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar /app/
RUN set -eux; \
    for f in /app/*.jar; do \
      case "$f" in *.original) ;; *) mv "$f" /app/app.jar ;; esac; \
    done; \
    rm -f /app/*.jar.original
EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]
