# syntax=docker/dockerfile:1.4

#############################
# Stage 1 – Build the JAR   #
#############################
ARG TARGETPLATFORM=linux/arm64
FROM --platform=${TARGETPLATFORM} gradle:7-jdk17 AS build

WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon

COPY src ./src
RUN ./gradlew build --no-daemon -x test

#############################
# Stage 2 – Run the JAR     #
#############################
FROM --platform=${TARGETPLATFORM} eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]