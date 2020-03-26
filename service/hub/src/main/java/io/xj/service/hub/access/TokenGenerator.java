// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.access;

public interface TokenGenerator {
  public String generate();

  public String generateShort();
}
