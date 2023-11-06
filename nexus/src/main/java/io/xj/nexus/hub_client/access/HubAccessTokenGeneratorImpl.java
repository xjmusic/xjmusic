// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.hub_client.access;


import java.util.UUID;

@Service
public class HubAccessTokenGeneratorImpl implements HubAccessTokenGenerator {
  @Override
  public String generate() {
    return System.currentTimeMillis() + "-" + UUID.randomUUID() + "-" + new StringBuilder(String.valueOf(System.nanoTime())).reverse();
  }
}

