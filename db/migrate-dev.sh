#!/usr/bin/env bash
set -e
BASEDIR=$(dirname "$0")/..

cd $BASEDIR
JDBC_URL="jdbc:hsqldb:hsql://localhost/gildedrose" ./gradlew flywayMigrate
