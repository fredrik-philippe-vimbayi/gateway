FROM maven:3.8.5-openjdk-17-slim as build
COPY ./ /src
RUN mvn -f /src/pom.xml clean package

FROM openjdk:17
COPY --from=build /src/target/gateway-service-0.0.1-SNAPSHOT.jar /src/
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "src/gateway-service-0.0.1-SNAPSHOT.jar"]
