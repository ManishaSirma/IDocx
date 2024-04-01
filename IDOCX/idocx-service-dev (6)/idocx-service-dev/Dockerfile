# Stage 1: Build the application
FROM maven:3.8.4-openjdk-17 as build
WORKDIR /app

# Copy the entire project
# Ensure this Dockerfile is located at the project root alongside the parent pom.xml
COPY . .

# Build the application from the parent POM
# This command builds all modules defined in your parent POM
RUN mvn clean package -DskipTests

# Stage 2: Create the final Docker image
FROM openjdk:17

# Copy the built jar from the specific module's target directory
COPY --from=build /app/idocx-service-core/target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
