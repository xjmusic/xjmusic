#!/usr/bin/env bash

# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

die () {
    echo >&2 "$@"
    exit 1
}

[ "$#" -eq 1 ] || die "1 argument required, $# provided"

for ID in $1;
do
  aws cloudfront create-invalidation --distribution-id ${ID} --paths "/*"
done
