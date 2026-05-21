FROM maven:3.8-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn package -DskipTests -B

FROM openjdk:17-jre-slim
WORKDIR /app

RUN groupadd -r blog && useradd -r -g blog blog

COPY --from=builder /app/target/*.jar app.jar

RUN chown -R blog:blog /app
USER blog

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/api/category/list || exit 1

ENTRYPOINT ["java", "-Xmx512m", "-Xms256m", "-jar", "app.jar"]
