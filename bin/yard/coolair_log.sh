#!/usr/bin/env bash

POD=$(kubectl -n yard get pods | grep -oe '^coolair[0-9a-zA-Z\-]*')

kubectl \
    -n yard \
    -c coolair \
    logs -f ${POD} \
    | grep -v '/healthz' \
    | sed -e 's/^.*WorkImpl//g'
