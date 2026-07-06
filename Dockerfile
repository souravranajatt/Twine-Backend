# ─── Stage 1: Build JAR ───────────────────────────

# Create Maven + Java image to build the JAR file
FROM maven:3.9.6-eclipse-temurin-21 AS builder

# Set working directory (Where code will be copied and built)
WORKDIR /app

# Copy pom.xml so app can download dependencies before copying the source code
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build the JAR file
COPY src ./src
RUN mvn clean package -DskipTests


# ─── Stage 2: Run JAR ─────────────────────────────

#Run the JAR file in a lightweight Java image
FROM eclipse-temurin:21-jre-alpine

# Set working directory to get souce code and run the JAR file
WORKDIR /app

# Copy the JAR file from the builder stage to the current working directory
COPY --from=builder /app/target/*.jar app.jar

# Expose the port that the application will run on
EXPOSE 8082

# Set the entry point to run the JAR file
ENTRYPOINT ["java", "-jar", "app.jar"]