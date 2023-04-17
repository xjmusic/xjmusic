#!/usr/bin/env bash

# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

error() {
  echo "Failed!"
  exit 1
}

# Connect as postgres
SQL_USER="postgres"
SQL_PASS="postgres"
SQL_PORT=5432
SQL_HOST="127.0.0.1"

# Database
SQL_DB_MAIN=${1:-xj_dev}

# Source
SOURCE_PREFIX="${PWD}/.backup/"
SOURCE_MAIN_RECORDS_SQL="${SOURCE_PREFIX}delete-and-insert-all-records.sql"

# Postgresql
export PGPASSWORD=postgres
CFG="--user=${SQL_USER} --host=${SQL_HOST} --port=${SQL_PORT}"

#
psql ${CFG} ${SQL_DB_MAIN}< ${SOURCE_MAIN_RECORDS_SQL} || error
