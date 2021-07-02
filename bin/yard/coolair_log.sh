#!/usr/bin/env bash

kubectl -n yard -c coolair logs -f deployment/coolair | grep -v '/-/health' | sed -e 's/^.*WorkImpl//g'

