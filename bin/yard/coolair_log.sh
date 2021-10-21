#!/usr/bin/env bash

# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

POD=$(kubectl -n yard get pods | grep -oe '^coolair[0-9a-zA-Z\-]*')

kubectl \
    -n yard \
    -c coolair \
    logs -f ${POD} \
    | grep -v '/healthz' \
    | sed -e 's/^.*WorkImpl//g'
