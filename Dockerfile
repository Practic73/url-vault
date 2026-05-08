FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests && \
    find target -maxdepth 1 -name "*.jar" ! -name "*-original.jar" -exec cp {} target/app.jar \;

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar
EXPOSE 8001
CMD ["java", "-jar", "app.jar"]
