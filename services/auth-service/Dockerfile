FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /workspace

COPY pom.xml pom.xml
COPY services/auth-service/pom.xml services/auth-service/pom.xml
COPY services/task-service/pom.xml services/task-service/pom.xml
COPY services/auth-service/src services/auth-service/src

RUN mvn -pl services/auth-service -am clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN useradd --uid 10001 --create-home --shell /usr/sbin/nologin springuser \
  && apt-get update \
  && apt-get install -y --no-install-recommends curl \
  && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/services/auth-service/target/auth-service-0.0.1-SNAPSHOT.jar app.jar

USER 10001

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
