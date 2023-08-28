FROM openjdk:11

COPY ./target/project_management-0.0.1-SNAPSHOT.jar /project_management.jar

EXPOSE 8080

CMD ["java", "-jar", "-Dspring.profiles.active=cloudv", "/project_management.jar"]