# Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#!/usr/bin/env bash

check() {
  TARGET=${1}
  if grep --recursive -Iie "\\btodo\\b" ${TARGET}
    then
      echo "There are remaining items TODO"
      exit 1;
    else
      echo "${TARGET} OK"
  fi
}

# Check if the first argument is "workstation" or "engine"
if [[ $1 == "workstation" || $1 == "engine" ]]; then
  check $1/src
  check $1/include
  check $1/test
else
  echo "Invalid argument. Please specify either 'workstation' or 'engine'."
