// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.persistence.kv;

import org.jetbrains.annotations.Nullable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryKvStore implements KvStore {
  final Map<String, String> store;

  public MemoryKvStore() {
    store = new ConcurrentHashMap<>();
  }

  @Override
  public void put(String key, String value) {
    store.put(key, value);
  }

  @Override
  public @Nullable String get(String key) {
    return store.containsKey(key) ? store.get(key) : null;
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
