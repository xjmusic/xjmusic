// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

@Primary
@Service
public class HubKvStoreProviderRedisImpl implements HubKvStoreProvider {
  private static final Logger LOG = LoggerFactory.getLogger(HubKvStoreProviderRedisImpl.class);
  private static String dbRedisHost;
  private static int dbRedisPort;
  private final EntityFactory entityFactory;

  public HubKvStoreProviderRedisImpl(AppEnvironment env, EntityFactory entityFactory) {
    dbRedisHost = env.getRedisHost();
    dbRedisPort = env.getRedisPort();
    this.entityFactory = entityFactory;
    LOG.info("Will connect to Redis host:{} port:{}", dbRedisHost, dbRedisPort);
  }

  /**
   * Redis server host, of config
   *
   * @return host
   */
  private static String host() {
    return dbRedisHost;
  }

  /**
   * Redis server port, of config
   *
   * @return port
   */
  private static int port() {
    return dbRedisPort;
  }


  /**
   * @return a Redis connection
   */
  private Jedis getClient() {
    return new Jedis(host(), port());
  }

  @Override
  public <T> void set(String key, T obj) throws HubPersistenceException {
    try (Jedis client = getClient()) {
      client.set(key, entityFactory.serialize(obj));
    } catch (Exception e) {
      LOG.error("While setting key {}", key, e);
      throw new HubPersistenceException("While setting key", e);
    }
  }

  @Override
  public <T> T get(Class<T> type, String key) throws HubPersistenceException {
    try (Jedis client = getClient()) {
      return entityFactory.deserialize(type, client.get(key));
    } catch (Exception e) {
      LOG.error("While getting key {}", key, e);
      throw new HubPersistenceException("While getting key", e);
    }
  }

  @Override
  public void del(String key) {
    try (Jedis client = getClient()) {
      client.del(key);
    } catch (Exception e) {
      LOG.warn("While deleting key {}", key, e);
    }
  }

  @Override
  public void clear() {
    try (Jedis client = getClient()) {
      client.flushAll();
    } catch (Exception e) {
      LOG.warn("While clearing keys", e);
    }
  }
}
