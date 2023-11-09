// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface JsonProvider {
  ObjectMapper getMapper();
}
