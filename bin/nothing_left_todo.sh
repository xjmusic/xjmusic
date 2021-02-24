# Copyright (c) Outright Mental. (https://outrightmental.com) All Rights Reserved.

#!/usr/bin/env bash

if grep --recursive -i todo src
  then
    echo "There are remaining items TODO"
    exit 1;
  else
    echo "OK"
fi