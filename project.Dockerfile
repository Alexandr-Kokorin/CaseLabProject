FROM eclipse-temurin:21.0.2_13-jdk-jammy AS builder
WORKDIR /opt/app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY project/pom.xml project/pom.xml
COPY .git/ .git/
RUN sed -i 's/\r$//' mvnw
RUN ./mvnw dependency:go-offline
COPY project/src/ project/src/
RUN ./mvnw -DskipTests=true clean install

FROM eclipse-temurin:21.0.2_13-jre-jammy AS final
WORKDIR /opt/app
EXPOSE 8080
COPY --from=builder /opt/app/project/target/*.jar /opt/app/*.jar
ENTRYPOINT ["java", "-jar", "/opt/app/*.jar"]
