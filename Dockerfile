FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/this-one-0.0.1-SNAPSHOT-standalone.jar /this-one/app.jar

EXPOSE 8080

CMD ["java", "-jar", "/this-one/app.jar"]
