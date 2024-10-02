FROM eclipse-temurin:21-jre-alpine
ARG JAR_FILE=project/target/project.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8080
