#!/usr/bin/env bash

# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

# Include common functions
. $( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )/../common/functions

# Connect as postgres
SQL_USER="postgres"
SQL_PORT=5432

export PGPASSWORD=postgres
psql --user=${SQL_USER} --host=127.0.0.1 --port=${SQL_PORT} "$@"
