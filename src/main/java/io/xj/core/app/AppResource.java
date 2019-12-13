// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.access.Access;
import io.xj.core.dao.DAO;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.payload.Payload;
import io.xj.core.transport.GsonProvider;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.core.work.WorkManager;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 A JAX-RS resource
 */
public class AppResource {
  protected final Config config;
  protected final HttpResponseProvider response;
  protected final GsonProvider gsonProvider;
  protected final WorkManager workManager;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public AppResource(
    Injector injector
  ) {
    response = injector.getInstance(HttpResponseProvider.class);
    gsonProvider = injector.getInstance(GsonProvider.class);
    workManager = injector.getInstance(WorkManager.class);
    config = injector.getInstance(Config.class);
  }

  /**
   Create one Entity via a DAO given a JSON:API payload request

   @param crc     request context
   @param dao     via which to of Entity
   @param payload of data to of Entity
   @param <N>     type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends Entity> Response create(ContainerRequestContext crc, DAO<N> dao, Payload payload) {
    try {
      Access access = Access.fromContext(crc);
      N createdEntity = dao.create(access, dao.newInstance().consume(payload));

      Payload responseData = new Payload();
      responseData.setDataOne(createdEntity.toPayloadObject());
      return response.create(responseData);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

  /**
   Read one Entity of a DAO and return the JSON:API payload response

   @param crc request context
   @param dao of which to read one Entity
   @param id  of Entity to read
   @param <N> type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends Entity> Response readOne(ContainerRequestContext crc, DAO<N> dao, Object id) {
    try {
      Entity entity = dao.readOne(Access.fromContext(crc), UUID.fromString(String.valueOf(id)));
      Payload payload = new Payload();
      payload.setDataEntity(entity);
      return response.ok(payload);

    } catch (CoreException ignored) {
      return response.notFound(dao.newInstance().setId(UUID.fromString(String.valueOf(id))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Read many Entity of a DAO and return the JSON:API payload response

   @param crc       request context
   @param dao       of which to read many Entity
   @param parentIds of Entity to read
   @param <N>       type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends Entity, O> Response readMany(ContainerRequestContext crc, DAO<N> dao, Collection<O> parentIds) {
    try {
      Collection<N> entities = dao.readMany(Access.fromContext(crc), parentIds.stream().map((Object name) -> UUID.fromString(String.valueOf(name))).collect(Collectors.toList()));
      Payload payload = new Payload();
      payload.setDataEntities(entities);
      return response.ok(payload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Read many Entity of a DAO and return the JSON:API payload response

   @param crc      request context
   @param dao      of which to read many Entity
   @param parentId of Entity to read
   @param <N>      type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends Entity> Response readMany(ContainerRequestContext crc, DAO<N> dao, String parentId) {
    if (Objects.isNull(parentId))
      return response.notAcceptable("parent id is required");

    return readMany(crc, dao, ImmutableList.of(parentId));
  }

  /**
   Update one Entity via a DAO given a JSON:API payload request

   @param crc     request context
   @param dao     via which to read one Entity
   @param id      of Entity to read
   @param payload of data to update
   @param <N>     type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends Entity> Response update(ContainerRequestContext crc, DAO<N> dao, String id, Payload payload) {
    try {
      Access access = Access.fromContext(crc);
      N current = dao.readOne(access, UUID.fromString(id));
      current.consume(payload);
      dao.update(access, UUID.fromString(id), current);
      return response.ok(new Payload().setDataEntity(current));

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one Entity via a DAO

   @param crc request context
   @param dao via which to delete one Entity
   @param id  of Entity to delete
   @param <N> type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends Entity> Response delete(ContainerRequestContext crc, DAO<N> dao, String id) {
    try {
      dao.destroy(Access.fromContext(crc), UUID.fromString(id));
      return response.noContent();

    } catch (Exception e) {
      return response.failure(e);
    }
  }
}
