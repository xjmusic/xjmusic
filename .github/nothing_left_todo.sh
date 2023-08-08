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

check hub/src
check lib/src
check nexus/src
check ship/src
