#!/usr/bin/env bash
set -e
BASEDIR=$(dirname "$0")/..

cd $BASEDIR
JDBC_URL="jdbc:h2:/tmp/gilded-rose.db" ./gradlew flywayMigrate
