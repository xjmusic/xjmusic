// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.entity.Entity;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.dao.DAO;
import io.xj.service.hub.dao.DAOException;

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
public class HubEndpoint {
  protected final Config config;
  protected final HttpResponseProvider response;
  protected final PayloadFactory payloadFactory;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public HubEndpoint(
    Injector injector
  ) {
    response = injector.getInstance(HttpResponseProvider.class);
    config = injector.getInstance(Config.class);
    payloadFactory = injector.getInstance(PayloadFactory.class);
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
      HubAccess hubAccess = HubAccess.fromContext(crc);
      N createdEntity = dao.create(hubAccess, payloadFactory.consume(dao.newInstance(), payload));

      Payload responseData = new Payload();
      responseData.setDataOne(payloadFactory.toPayloadObject(createdEntity));
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
      Entity entity = dao.readOne(HubAccess.fromContext(crc), UUID.fromString(String.valueOf(id)));
      Payload payload = new Payload();
      payload.setDataOne(payloadFactory.toPayloadObject(entity));
      return response.ok(payload);

    } catch (DAOException ignored) {
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
      Collection<N> entities = dao.readMany(HubAccess.fromContext(crc), parentIds.stream().map((Object name) -> UUID.fromString(String.valueOf(name))).collect(Collectors.toList()));
      Payload payload = new Payload().setDataType(PayloadDataType.Many);
      payload.setDataType(PayloadDataType.Many);
      for (N entity : entities) payload.addData(payloadFactory.toPayloadObject(entity));
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
      HubAccess hubAccess = HubAccess.fromContext(crc);
      N current = dao.readOne(hubAccess, UUID.fromString(id));
      payloadFactory.consume(current, payload);
      dao.update(hubAccess, UUID.fromString(id), current);
      return response.ok(new Payload().setDataOne(payloadFactory.toPayloadObject(current)));

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
      dao.destroy(HubAccess.fromContext(crc), UUID.fromString(id));
      return response.noContent();

    } catch (Exception e) {
      return response.failure(e);
    }
  }
}
