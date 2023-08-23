#!/bin/bash

set -e

INSTALLER_TYPE=${1}
INPUT=${2}
OUTPUT=${3}
JAR=${4}
VERSION=${5}
APP_ICON=${6}
EXTRA_BUNDLER_ARGUMENTS=${7}

jpackage \
  --type "${INSTALLER_TYPE}" \
  --verbose \
  --input "${INPUT}" \
  --dest "${OUTPUT}" \
  --name "XJ Music WorkStation" \
  --main-jar "${JAR}" \
  --app-version "${VERSION}" \
  --icon "$APP_ICON" \
  $EXTRA_BUNDLER_ARGUMENTS \
