#/bin/bash
set -e
mvn package
MAVEN_OPTS="-ea" mvn exec:java

