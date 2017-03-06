// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.util.token;

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

