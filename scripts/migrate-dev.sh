#!/usr/bin/env bash
set -e
BASEDIR=$(dirname "$0")/..

cd $BASEDIR
./gradlew flywayMigrateDev
