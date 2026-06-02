# ===== STAGE 1: BUILD =====
# Use a Maven image with Java 17 to compile the code
FROM maven:3.9-amazoncorretto-17 AS build

# Set working directory inside container
WORKDIR /app

# Copy pom.xml FIRST (Docker caches this layer — dependencies won't re-download if only source changes)
COPY pom.xml .

# Then copy your actual Java source
COPY src ./src

# Compile and package into a JAR file (skip tests for speed)
RUN mvn package -DskipTests -B -T 4 -q

# ===== STAGE 2: RUNTIME =====
# Start fresh from a smaller image (JRE only, ~150MB vs ~400MB for Maven image)
FROM amazoncorretto:17-alpine

WORKDIR /app

# Install Tesseract OCR — needed by your PDF report analyzer
RUN apk add --no-cache \
    tesseract-ocr \
    tesseract-ocr-data-eng

# Copy ONLY the JAR from the build stage (no Maven, no source code)
COPY --from=build /app/target/*.jar app.jar

# Document that this container listens on port 8080
EXPOSE 8080

# Command that runs when container starts
ENTRYPOINT ["java", "-jar", "app.jar"]