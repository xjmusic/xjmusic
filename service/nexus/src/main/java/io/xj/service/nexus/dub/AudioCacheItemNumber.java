// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub;

import java.util.concurrent.atomic.AtomicInteger;

public enum AudioCacheItemNumber {
  ONE;

  private static final Integer NEXT_DELTA = 1;
  private final AtomicInteger atomicInteger;

  AudioCacheItemNumber() {
    atomicInteger = new AtomicInteger();
  }

  private Integer addAndGet() {
    return atomicInteger.addAndGet(NEXT_DELTA);
  }

  public static Integer next() {
    return ONE.addAndGet();
  }

}
