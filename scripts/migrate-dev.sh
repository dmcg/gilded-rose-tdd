#!/usr/bin/env bash
set -e
BASEDIR=$(dirname "$0")/..

cd $BASEDIR
JDBC_URL='jdbc:postgresql://localhost:5432/gilded-rose' ./gradlew flywayMigrate
