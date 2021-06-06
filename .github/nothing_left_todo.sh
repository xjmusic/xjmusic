# Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#!/usr/bin/env bash

check() {
  TARGET=${1}
  if grep --recursive -i todo ${TARGET}
    then
      echo "There are remaining items TODO"
      exit 1;
    else
      echo "${TARGET} OK"
  fi
}

check src
