FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /build

COPY pom.xml ./
COPY videoapp-web/pom.xml videoapp-web/
COPY videoapp-common/pom.xml videoapp-common/
COPY videoapp-storage/pom.xml videoapp-storage/

RUN mvn dependency:go-offline

COPY videoapp-web videoapp-web
COPY videoapp-common videoapp-common
COPY videoapp-storage videoapp-storage

RUN mvn clean package -X

FROM eclipse-temurin:17-jdk-alpine AS videoapp-web
WORKDIR /app

COPY --from=builder /build/videoapp-web/target/videoapp-web.jar videoapp-web.jar
EXPOSE 8080 5005

ENTRYPOINT ["sh", "-c", "java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 -jar videoapp-web.jar"]
