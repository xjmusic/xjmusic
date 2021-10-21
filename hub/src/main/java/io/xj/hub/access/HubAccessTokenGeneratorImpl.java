// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import java.util.UUID;

class HubAccessTokenGeneratorImpl implements HubAccessTokenGenerator {
  @Override
  public String generate() {
    return System.currentTimeMillis() + "-" + UUID.randomUUID() + "-" + new StringBuilder(String.valueOf(System.nanoTime())).reverse();
  }
}

