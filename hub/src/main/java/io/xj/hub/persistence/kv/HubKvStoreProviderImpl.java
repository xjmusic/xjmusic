// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence.kv;

import io.xj.hub.persistence.HubPersistenceException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.ValueUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Objects;

@Primary
@Service
public class HubKvStoreProviderImpl implements HubKvStoreProvider {
  static final Logger LOG = LoggerFactory.getLogger(HubKvStoreProviderImpl.class);
  final EntityFactory entityFactory;
  final KvStore store;
  final String memcacheAddress;
  final int memcacheExpirationSeconds;

  @Autowired
  public HubKvStoreProviderImpl(
    EntityFactory entityFactory,
    @Value("${memcache.address}")
    String memcacheAddress,
    @Value("${memcache.expiration.seconds}")
    int memcacheExpirationSeconds) {
    store = createStore(memcacheAddress);
    this.entityFactory = entityFactory;
    this.memcacheAddress = memcacheAddress;
    this.memcacheExpirationSeconds = memcacheExpirationSeconds;
    LOG.info("Will use local in-memory store");
  }

  @Override
  public <T> void set(String key, T obj) throws HubPersistenceException {
    try {
      store.put(key, entityFactory.serialize(obj));
    } catch (Exception e) {
      LOG.error("While setting key {}", key, e);
      throw new HubPersistenceException("While setting key", e);
    }
  }

  @Override
  public <T> @Nullable T get(Class<T> type, String key) throws HubPersistenceException {
    try {
      var value = store.get(key);
      return Objects.nonNull(value) ? entityFactory.deserialize(type, value) : null;
    } catch (Exception e) {
      LOG.error("While getting key {}", key, e);
      throw new HubPersistenceException("While getting key", e);
    }
  }

  @Override
  public void del(String key) {
    try {
      store.remove(key);
    } catch (Exception e) {
      LOG.warn("While deleting key {}", key, e);
    }
  }

  @Override
  public void clear() {
    try {
      store.clear();
    } catch (Exception e) {
      LOG.warn("While clearing keys", e);
    }
  }

  @Override
  public void shutdown() {
    try {
      store.shutdown();
    } catch (IOException e) {
      LOG.warn("While shutting down store", e);
    }
  }

  /**
   * Create a KV store. If provided, try to connect to memcache. If that fails or no memcache address is specified, use an in-memory store.
   *
   * @param memcacheAddress memcache address
   * @return KV store
   */
  KvStore createStore(String memcacheAddress) {
    if (!ValueUtils.isEmpty(memcacheAddress)) {
      try {
        return new MemcacheKvStore(memcacheAddress, memcacheExpirationSeconds);
      } catch (IOException e) {
        LOG.warn("Unable to connect to memcache at {}", memcacheAddress, e);
      }
    }
    return new MemoryKvStore();
  }
}
