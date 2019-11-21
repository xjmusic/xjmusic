// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access.token;

public interface TokenGenerator {
  public String generate();

  public String generateShort();
}
