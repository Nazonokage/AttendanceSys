FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src src
RUN ./mvnw -B package -DskipTests
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN apk add --no-cache fontconfig ttf-dejavu && mkdir -p /app/data
COPY --from=build /build/target/*.jar app.jar
ENV ATTENDANCE_DB_URL=jdbc:sqlite:/app/data/attendance.db
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]
