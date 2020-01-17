#!/bin/bash -x

export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
mvn  -DskipTests=true  package hpi:run

