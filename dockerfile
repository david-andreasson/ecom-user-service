# Steg 1: Bygg applikationen med Maven och Corretto 21
FROM maven:3.9.6-amazoncorretto-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -Dmaven.test.skip=true

# Steg 2: Kör applikationen med Amazon Corretto 21
FROM amazoncorretto:21
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080

RUN mkdir -p /app/data
VOLUME ["/app/data"]

ENTRYPOINT ["java", "-jar", "app.jar"]