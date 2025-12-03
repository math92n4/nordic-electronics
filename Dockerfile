FROM eclipse-temurin:17-jdk

# Set work directory
WORKDIR /app

# Copy pom.xml and download dependencies (cached)
COPY pom.xml .
COPY mvnw ./
COPY mvnw.cmd ./
COPY .mvn .mvn/
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline

# Copy the entire source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port 8080
EXPOSE 8080

# Run the built JAR file
ENTRYPOINT ["java", "-jar", "target/nordic-electronics-0.0.1-SNAPSHOT.jar"]
