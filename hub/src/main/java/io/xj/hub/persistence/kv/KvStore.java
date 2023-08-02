// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.persistence.kv;

import javax.annotation.Nullable;
import java.io.IOException;

public interface KvStore {
  void put(String key, String value);

  @Nullable
  String get(String key);

  void remove(String key);

  void clear();

  void shutdown() throws IOException;
}
