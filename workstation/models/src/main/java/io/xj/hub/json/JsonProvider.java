// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface JsonProvider {
  ObjectMapper getMapper();
}
