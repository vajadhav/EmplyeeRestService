FROM jboss/base-jdk:8
MAINTAINER markus@jboss.org
EXPOSE 8765
ADD EmployeeCouchAkkaService-1.0-SNAPSHOT-allinone.jar /opt/akka-http/akka-http-employee-service.jar
USER root
RUN chown -R jboss:jboss /opt/akka-http/
USER jboss
RUN sh -c 'touch /opt/akka-http/akka-http-employee-service.jar'
CMD java -Djava.security.egd=file:/dev/./urandom -jar /opt/akka-http/akka-http-employee-service.jar