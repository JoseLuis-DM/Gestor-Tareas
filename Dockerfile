FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y netcat && rm -rf /var/lib/apt/lists/*

ARG JAR_FILE=target/gestor-de-tareas-0.0.1.jar
COPY ${JAR_FILE} app_gestor.jar

COPY wait-for.sh wait-for.sh
RUN chmod +x wait-for.sh

EXPOSE 8080

ENV DB_HOST=gestor \
    DB_PORT=3306

ENTRYPOINT ["./wait-for.sh", "gestor-db", "3306", "java", "-jar", "app_gestor.jar"]