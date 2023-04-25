#!/usr/bin/env bash

# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# Requires 6 arguments
if [ $# != 6 ]; then echo "
   Usage:
     bin/sql/backup/records <user> <password> <database> <host> <port> <path/to/output.sql>

"
fi
SQL_USER="${1}"
SQL_DB="${3}"
SQL_HOST="${4}"
SQL_PORT="${5}"
OUTPUT_SQL="${6}"

#
EXCLUDED_TABLES=(
xj.flyway_schema_history
)

#
IGNORED_TABLES_STRING=''
for TABLE in "${EXCLUDED_TABLES[@]}"
do :
   IGNORE_TABLE=" --exclude-table-data=${TABLE}"
   echo "${IGNORE_TABLE}"
   IGNORED_TABLES_STRING+="${IGNORE_TABLE}"
done

# Tables
COPYRIGHT="Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved."

# empty target file
printf "\-\- ${COPYRIGHT}\n\n" > ${OUTPUT_SQL}

# then append data from only the schema version table
export PGPASSWORD=postgres
pg_dump -a \
  --username=${SQL_USER} \
  --host=${SQL_HOST} \
  --port=${SQL_PORT} \
  ${IGNORED_TABLES_STRING} \
  --dbname=${SQL_DB} >> ${OUTPUT_SQL}
