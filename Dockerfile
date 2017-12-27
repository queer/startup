FROM openjdk:8-jre-alpine

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/startup/startup.jar"]

ADD target/startup*.jar /usr/share/startup/startup.jar