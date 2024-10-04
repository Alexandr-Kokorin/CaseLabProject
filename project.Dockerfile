FROM maven:3.9.7-eclipse-temurin-21 AS builder
WORKDIR /opt/app
COPY pom.xml .
COPY project/pom.xml project/pom.xml
COPY .git/ .git/
COPY project/src/ project/src/
RUN mvn dependency:go-offline
RUN mvn clean install -DskipTests

FROM eclipse-temurin:21.0.2_13-jre-jammy AS final
WORKDIR /opt/app
EXPOSE 8080
COPY --from=builder /opt/app/project/target/*.jar /opt/app/*.jar
ENTRYPOINT ["java", "-jar", "/opt/app/*.jar"]
