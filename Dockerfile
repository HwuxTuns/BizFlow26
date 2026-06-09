# File: Dockerfile
# M� t?: File c?u h�nh Docker d? build image Spring Boot
# Ch?c nang: T?o image Docker t? source code Java,
#            c�i d?t dependencies, compile code, build JAR file,
#            ch?y ?ng d?ng Spring Boot trong container

# Stage 1: Build the application using Maven and Java 21
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY BizFlow.AppService/pom.xml BizFlow.AppService/pom.xml
COPY BizFlow.PromotionService/pom.xml BizFlow.PromotionService/pom.xml
COPY BizFlow.AppService/src BizFlow.AppService/src
COPY BizFlow.PromotionService/src BizFlow.PromotionService/src
RUN mvn -pl BizFlow.AppService -am -DskipTests package

# Stage 2: Create the final, lightweight image with the application JAR
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/BizFlow.AppService/target/*.jar /app/
RUN set -eux; \
    for f in /app/*.jar; do \
      case "$f" in *.original) ;; *) mv "$f" /app/app.jar ;; esac; \
    done; \
    rm -f /app/*.jar.original
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

