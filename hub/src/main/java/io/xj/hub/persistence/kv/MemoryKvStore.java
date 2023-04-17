// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.persistence.kv;

import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;

public class MemoryKvStore implements KvStore {
  private final Map<String, String> store;

  public MemoryKvStore() {
    store = Maps.newConcurrentMap();
  }

  @Override
  public void put(String key, String value) {
    store.put(key, value);
  }

  @Override
  public String get(String key) {
    return store.get(key);
  }

  @Override
  public void remove(String key) {
    store.remove(key);
  }

  @Override
  public void clear() {
    store.clear();
  }

  @Override
  public void shutdown() throws IOException {
    // no op
  }
}
