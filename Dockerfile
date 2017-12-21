FROM openjdk:8-jre-alpine

ENTRYPOINT ["/usr/bin/java", "-jar", "/usr/share/lily/lily.jar"]

ADD target/lily*.jar /usr/share/lily/lily.jar