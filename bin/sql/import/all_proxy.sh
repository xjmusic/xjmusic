#!/usr/bin/env bash

# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

USAGE="
         Usage:
           bin/sql/import/all_local.sh <db_name> <db_user> <db_pass>
      "

# Require first argument: database name
if [ $# -lt 1 ]; then echo ${USAGE}; exit 1; fi
SQL_DB=${1}

# Require second argument: database user
if [ $# -lt 2 ]; then echo ${USAGE}; exit 1; fi
SQL_USER=${2}

# Require third argument: database password
if [ $# -lt 3 ]; then echo ${USAGE}; exit 1; fi
SQL_PASS=${3}

error() {
  echo "Failed!"
  exit 1
}

# Connect as postgres
SQL_PORT=5432
SQL_HOST="127.0.0.1"

# Source
SOURCE_PREFIX="${PWD}/.backup/"
SOURCE_MAIN_RECORDS_SQL="${SOURCE_PREFIX}delete-and-insert-all-records.sql"

# Postgresql
export PGPASSWORD=${SQL_PASS}
CFG="--user=${SQL_USER} --host=${SQL_HOST} --port=${SQL_PORT}"

#
psql ${CFG} ${SQL_DB}< ${SOURCE_MAIN_RECORDS_SQL} || error
