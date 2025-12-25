FROM eclipse-temurin:17-alpine
LABEL maintainer="BookshopStudent"
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]