// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import com.google.common.collect.Maps;
import io.xj.lib.entity.EntityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.Map;

@Primary
@Service
public class HubKvStoreProviderImpl implements HubKvStoreProvider {
  private static final Logger LOG = LoggerFactory.getLogger(HubKvStoreProviderImpl.class);
  private final EntityFactory entityFactory;
  private final Map<String, String> store = Maps.newConcurrentMap();

  public HubKvStoreProviderImpl(EntityFactory entityFactory) {
    this.entityFactory = entityFactory;
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
}
