# Build with maven
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

RUN apt-get update && apt-get install -y netcat && rm -rf /var/lib/apt/lists/*

COPY --from=builder /app/target/*.jar app_gestor.jar
COPY wait-for.sh wait-for.sh
RUN chmod +x wait-for.sh

ENV SPRING_PROFILES_ACTIVE=dev \
    JAVA_OPTS="-Xms128m -Xmx512m" \
    TZ=America/Mexico_City \
    DB_HOST=gestor-db \
    DB_PORT=3306

EXPOSE 8080

ENTRYPOINT ["./wait-for.sh", "gestor-db", "3306", "--", "sh", "-c", "java $JAVA_OPTS -jar app_gestor.jar"]