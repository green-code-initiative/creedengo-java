# ARG MAVEN_BUILDER=3-openjdk-11-slim
ARG MAVEN_BUILDER=3-openjdk-17-slim

# ARG SONARQUBE_VERSION=9.9.8-community
#ARG SONARQUBE_VERSION=9.9.0-community
#ARG SONARQUBE_VERSION=24.12.0.100206-community
#ARG SONARQUBE_VERSION=25.1.0.102122-community
#ARG SONARQUBE_VERSION=25.2.0.102705-community
#ARG SONARQUBE_VERSION=25.3.0.104237-community
ARG SONARQUBE_VERSION=25.9.0.112764-community

FROM maven:${MAVEN_BUILDER} AS builder

COPY . /usr/src/creedengo

WORKDIR /usr/src/creedengo
COPY src src/
COPY pom.xml tool_build.sh ./

RUN ./tool_build.sh

FROM sonarqube:${SONARQUBE_VERSION}

COPY --from=builder /usr/src/creedengo/target/creedengo-*.jar /opt/sonarqube/extensions/plugins/

# Install the ca-certificate package
USER root
# RUN apt-get update && apt-get install -y ca-certificates
# Copy SSL certificates to the container
COPY downloads-sonarsource.crt /usr/local/share/ca-certificates/
# Update SSL certificates in system inside the container
# RUN update-ca-certificates

## Update SSL certificates in the JDK inside the container
RUN $JAVA_HOME/bin/keytool -import -trustcacerts -file /usr/local/share/ca-certificates/downloads-sonarsource.crt -alias downloads-sonarsource -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt

## Process manuel
# GENERATION CERTIFICAT
# openssl s_client -showcerts -connect downloads.sonarsource.com:443 </dev/null 2>/dev/null | openssl x509 > downloads-sonarsource.crt
# COPIE CERTIFICAT SUR CONTENEUR
# dk cp downloads-sonarsource.crt sonar_creedengo_java:/tmp/.
# AJOUT CERTIFICAT DANS LE KEYSTORE (en root)
# dk exec -u root -it sonar_creedengo_java /bin/bash
# $JAVA_HOME/bin/keytool -import -trustcacerts -file /tmp/downloads-sonarsource.crt -alias downloads-sonarsource -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt
# RELANCE CONTENEUR pour relancer le service sonarqube

USER sonarqube
