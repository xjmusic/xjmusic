// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface JsonProvider {
  ObjectMapper getObjectMapper();
}
