# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#!/usr/bin/env bash

check() {
  TARGET=${1}
  if grep --recursive -ie "\\btodo\\b" ${TARGET}
    then
      echo "There are remaining items TODO"
      exit 1;
    else
      echo "${TARGET} OK"
  fi
}

check build.gradle

check hub/build.gradle
check hub/src

check lib/build.gradle
check lib/src

check nexus/build.gradle
check nexus/src

check spec/build.gradle
check spec/src

check .ops
