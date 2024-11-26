# Step 1: Build phase
FROM eclipse-temurin:21-jdk-jammy as build
WORKDIR /app
# Install Maven
RUN apt update -y && apt upgrade -y && apt install -y maven
COPY pom.xml ./
COPY src ./src
RUN mvn clean install

# Step 2: Runtime phase
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar YEPPBot.jar
EXPOSE 420
ENTRYPOINT ["java", "-jar", "YEPPBot.jar", "-cli"]