// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence.kv;

import io.xj.hub.persistence.HubPersistenceException;

import org.jetbrains.annotations.Nullable;

public interface HubKvStoreProvider {

  /**
   * Encapsulate the set operation
   *
   * @param key key to set
   * @param obj object to set
   * @param <T> type of object
   * @throws HubPersistenceException if there is an error
   */
  <T> void set(String key, T obj) throws HubPersistenceException;

  /**
   * Encapsulate the get operation
   *
   * @param <T>  type of object
   * @param type type of object
   * @param key  key to get
   * @return object
   * @throws HubPersistenceException if there is an error
   */
  <T> @Nullable T get(Class<T> type, String key) throws HubPersistenceException;

  /**
   * Encapsulate the del operation
   *
   * @param key key to delete
   */
  void del(String key);

  /**
   * Clear all keys
   */
  void clear();

  /**
   * Shutdown the store
   */
  void shutdown();
}
