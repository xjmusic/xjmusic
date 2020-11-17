// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.testing;

import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.jsonapi.JsonApiException;

import java.util.Collection;

/**
 An Integration test needs to:
 1. create injector with a Config binding
 2. inject IntegrationTestProvider
 AND
 3. call integrationTestProvider.shutdown()
 */
public interface NexusIntegrationTestProvider {

  /**
   Runs on program exit
   */
  void tearDown();

  /**
   Reset the database before an integration test.
   */
  void setUp();

  /**
   Put an entity into the record store

   @param entity to put
   @param <E>    type of entity
   @return entity (for chaining methods)
   */
  <E> E put(E entity) throws JsonApiException, EntityStoreException;

  /**
   Put an entity into the record store, including child entities

   @param <E>      type of entity
   @param <I>      types of child entities
   @param entity   record to put
   @param included entities to put with the record
   @return entity (for chaining methods)
   */
  <E, I> E put(E entity, Collection<I> included) throws JsonApiException, EntityStoreException;
}
