# Build Promotion Service
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B || true
COPY src src
RUN mvn -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# The promotion-service pom uses spring-boot-maven-plugin with classifier "exec",
# so the runnable jar is *-exec.jar (the plain jar has no main manifest).
COPY --from=build /app/target/*-exec.jar /app/app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
