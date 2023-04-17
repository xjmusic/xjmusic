// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence.kv;

import io.xj.hub.persistence.HubPersistenceException;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactory;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Primary
@Service
public class HubKvStoreProviderImpl implements HubKvStoreProvider {
  private static final Logger LOG = LoggerFactory.getLogger(HubKvStoreProviderImpl.class);
  private final EntityFactory entityFactory;
  private final KvStore store;
  private final int memcacheExpirationSeconds;

  public HubKvStoreProviderImpl(AppEnvironment env, EntityFactory entityFactory) {
    store = createStore(env.getMemcacheAddress());
    this.entityFactory = entityFactory;
    LOG.info("Will use local in-memory store");
    memcacheExpirationSeconds = env.getMemcacheExpirationSeconds();
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
  public <T> T get(Class<T> type, String key) throws HubPersistenceException {
    try {
      return entityFactory.deserialize(type, store.get(key));
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
  private KvStore createStore(String memcacheAddress) {
    if (!Strings.isEmpty(memcacheAddress)) {
      try {
        return new MemcacheKvStore(memcacheAddress, memcacheExpirationSeconds);
      } catch (IOException e) {
        LOG.warn("Unable to connect to memcache at {}", memcacheAddress, e);
      }
    }
    return new MemoryKvStore();
  }
}
