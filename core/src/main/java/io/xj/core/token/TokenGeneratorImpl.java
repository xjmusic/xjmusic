// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.token;

import java.util.UUID;

public class TokenGeneratorImpl implements TokenGenerator {
  @Override
  public String generate() {
    return String.valueOf(System.currentTimeMillis()) + "-" + UUID.randomUUID().toString() + "-" + new StringBuilder(String.valueOf(System.nanoTime())).reverse().toString();
  }

  @Override
  public String generateShort() {
    return UUID.randomUUID().toString();
  }
}

