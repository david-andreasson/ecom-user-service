# Steg 1: Bygg applikationen med Maven och Corretto 21
FROM maven:3.9.6-amazoncorretto-21 AS builder
WORKDIR /app

# Kopiera endast pom först för att cacha dependencies
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -DskipTests \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -Dmaven.wagon.http.pool=false \
    dependency:go-offline

# Kopiera resten och bygg
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -q -B -Dmaven.test.skip=true \
    -Dmaven.wagon.http.retryHandler.count=5 \
    -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
    -Dmaven.wagon.http.pool=false \
    clean package

# Steg 2: Kör applikationen med Amazon Corretto 21
FROM amazoncorretto:21
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080

RUN mkdir -p /app/data
VOLUME ["/app/data"]

ENTRYPOINT ["java", "-jar", "app.jar"]