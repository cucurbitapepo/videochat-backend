FROM gradle:8.14-jdk17 AS builder

WORKDIR /home/gradle/project

COPY build.gradle settings.gradle ./
COPY src/main/resources ./src/main/resources/

RUN gradle build -x test --no-daemon || return 0

COPY . .

RUN gradle clean bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "app.jar"]