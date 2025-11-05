FROM eclipse-temurin:17-jdk

# Set work directory
WORKDIR /app

# Copy pom.xml and download dependencies (cached)
COPY pom.xml .
COPY mvnw ./
COPY .mvn .mvn/
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline

# Copy the entire source code
COPY src ./src

# Expose port 8080
EXPOSE 8080

# Run Spring Boot in dev mode (restart + live reload)
ENTRYPOINT ["./mvnw", "spring-boot:run"]
