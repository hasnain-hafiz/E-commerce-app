# Stage 1: Build the JAR
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY E-commerce-app/pom.xml .
COPY E-commerce-app/src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run the JAR
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
