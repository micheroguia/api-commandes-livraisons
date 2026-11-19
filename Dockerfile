# Étape 1 : on compile l'application avec Maven
FROM maven:3.9-eclipse-temurin-17 AS construction
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# Étape 2 : une image légère qui contient seulement Java et le .jar
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN useradd --system --no-create-home api
USER api
COPY --from=construction /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
