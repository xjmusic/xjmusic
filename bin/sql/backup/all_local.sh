#!/usr/bin/env bash

# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# Include common functions
. $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/../../common/functions

# Connect as postgres
SQL_USER="postgres"
SQL_PASS="postgres"
SQL_PORT=5432
SQL_HOST="127.0.0.1"

# Commands
CMD_BACKUP_RECORDS="bin/sql/backup/records"

# Database
DB_MAIN=${1:-xj_dev}

# Output
OUTPUT_PREFIX="${PWD}/.backup/"
OUTPUT_MAIN_RECORDS_SQL="${OUTPUT_PREFIX}delete-and-insert-all-records.sql"

#
step "Backup main database records"
echo "    ${CMD_BACKUP_RECORDS} ${SQL_USER} ${SQL_PASS} ${DB_MAIN} ${SQL_HOST} ${SQL_PORT} ${OUTPUT_MAIN_RECORDS_SQL}"
${CMD_BACKUP_RECORDS} "${SQL_USER}" "${SQL_PASS}" "${DB_MAIN}" "${SQL_HOST}" "${SQL_PORT}" "${OUTPUT_MAIN_RECORDS_SQL}"
step_ok

#
finished_ok
