# Step 1: Build phase
FROM eclipse-temurin:21-jdk-jammy as build
WORKDIR /app

# Install Maven
RUN apt update -y && apt upgrade -y && apt install -y maven

# Copy the pom.xml and source files
COPY pom.xml ./
COPY src ./src

RUN mvn clean install -DskipTests

# Step 2: Runtime phase
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# Copy the built jar file from the build phase
COPY --from=build /app/target/YEPPBot-*.jar YEPPBot.jar

# Expose port
EXPOSE 420
EXPOSE 8000

# Run the application
ENTRYPOINT ["java", "-jar", "YEPPBot.jar", "-cli", "-ni"]