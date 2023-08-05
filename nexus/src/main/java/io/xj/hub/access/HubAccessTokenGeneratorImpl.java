// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.hub.access;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class HubAccessTokenGeneratorImpl implements HubAccessTokenGenerator {
  @Override
  public String generate() {
    return System.currentTimeMillis() + "-" + UUID.randomUUID() + "-" + new StringBuilder(String.valueOf(System.nanoTime())).reverse();
  }
}

