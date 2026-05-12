FROM gradle:8.14-jdk17 AS builder

WORKDIR /home/gradle/project

COPY build.gradle settings.gradle ./

RUN gradle dependencies --no-daemon || return 0

COPY src ./src

RUN gradle clean bootJar -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /home/gradle/project/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]