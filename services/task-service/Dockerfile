FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml pom.xml
COPY services/task-service/pom.xml services/task-service/pom.xml
COPY services/auth-service/pom.xml services/auth-service/pom.xml
COPY services/task-service/src services/task-service/src

RUN mvn -pl services/task-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd --uid 10001 --create-home --shell /usr/sbin/nologin springuser

COPY --from=build /workspace/services/task-service/target/task-service-*.jar app.jar

USER 10001

EXPOSE 8082

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
