// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.persistence.kv;


import net.spy.memcached.AddrUtil;
import net.spy.memcached.MemcachedClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

public class MemcacheKvStore implements KvStore {
  static final Logger LOG = LoggerFactory.getLogger(MemcacheKvStore.class);
  final MemcachedClient memcachedClient;
  final int expirationSeconds;

  public MemcacheKvStore(String memcacheAddress, int expirationSeconds) throws IOException {
    memcachedClient = new MemcachedClient(AddrUtil.getAddresses(memcacheAddress));
    this.expirationSeconds = expirationSeconds;
    LOG.info("Created memcache client OK, connected to {}", memcacheAddress);
  }

  @Override
  public void put(String key, String value) {
    memcachedClient.set(key, expirationSeconds, value);
  }

  @Override
  public @Nullable String get(String key) {
    var value = memcachedClient.get(key);
    return Objects.nonNull(value) ? value.toString() : null;
  }

  @Override
  public void remove(String key) {
    memcachedClient.delete(key);
  }

  @Override
  public void clear() {
    LOG.warn("Will not clear memcache");
  }

  @Override
  public void shutdown() throws IOException {
    memcachedClient.shutdown();
    LOG.info("Shut down memcache client OK");
  }
}
