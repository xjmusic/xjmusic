// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.access;

import java.util.UUID;

class TokenGeneratorImpl implements TokenGenerator {
  @Override
  public String generate() {
    return String.valueOf(System.currentTimeMillis()) + "-" + UUID.randomUUID().toString() + "-" + new StringBuilder(String.valueOf(System.nanoTime())).reverse().toString();
  }

  @Override
  public String generateShort() {
    return UUID.randomUUID().toString();
  }
}

