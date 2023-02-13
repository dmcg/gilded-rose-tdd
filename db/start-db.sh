#!/usr/bin/env bash
set -e
BASEDIR=$(dirname "$0")

docker-compose --file ${BASEDIR}/docker-compose.yml up
