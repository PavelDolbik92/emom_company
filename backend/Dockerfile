FROM maven:3.6.3-openjdk-17-slim AS build
WORKDIR /app
COPY . .
RUN mvn -f pom.xml -DskipTests clean package

FROM openjdk:17-jdk-alpine
COPY --from=build /app/target/cfprogram.emom-company-1.0.0.jar /usr/local/lib/app.jar

ENTRYPOINT ["java", "-jar", "/usr/local/lib/app.jar"]
