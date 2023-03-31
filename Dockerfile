FROM openjdk:11

COPY ./target/project_management-0.0.1-SNAPSHOT.jar /project_management.jar

CMD ["/usr/bin/java", "-jar", "/project_management.jar"]