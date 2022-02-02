// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.Manager;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.PayloadDataType;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 A JAX-RS resource
 */
public class HubJsonapiEndpoint<E> extends HubPersistenceServiceImpl<E> {
  public static final String ADMIN = "Admin";
  public static final String ARTIST = "Artist";
  //  public static final String BANNED = "Banned";
  public static final String ENGINEER = "Engineer";
  public static final String INTERNAL = "Internal";
  public static final String USER = "User";
  protected final JsonapiHttpResponseProvider response;
  protected final JsonapiPayloadFactory payloadFactory;

  /**
   Constructor
   */
  @Inject
  public HubJsonapiEndpoint(
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(entityFactory, dbProvider);
    this.response = response;
    this.payloadFactory = payloadFactory;
  }

  /**
   Create one Entity via a Manager given a JSON:API payload request

   @param crc            request context
   @param manager        via which to of Entity
   @param jsonapiPayload of data to of Entity
   @param <N>            type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N> Response create(ContainerRequestContext crc, Manager<N> manager, JsonapiPayload jsonapiPayload) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      N createdEntity = manager.create(hubAccess, payloadFactory.consume(manager.newInstance(), jsonapiPayload));

      JsonapiPayload responseData = new JsonapiPayload();
      responseData.setDataOne(payloadFactory.toPayloadObject(createdEntity));
      return response.create(responseData);

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }

  /**
   Read one Entity of a Manager and return the JSON:API payload response

   @param crc request context
   @param manager of which to read one Entity
   @param id  of Entity to read
   @param <N> type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N> Response readOne(ContainerRequestContext crc, Manager<N> manager, Object id) {
    try {
      Object entity = manager.readOne(HubAccess.fromContext(crc), UUID.fromString(String.valueOf(id)));
      JsonapiPayload jsonapiPayload = new JsonapiPayload();
      jsonapiPayload.setDataOne(payloadFactory.toPayloadObject(entity));
      return response.ok(jsonapiPayload);

    } catch (ManagerException ignored) {
      return response.notFound(manager.newInstance().getClass(), UUID.fromString(String.valueOf(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Read many Entity of a Manager and return the JSON:API payload response

   @param crc       request context
   @param manager       of which to read many Entity
   @param parentIds of Entity to read
   @param <N>       type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N, O> Response readMany(ContainerRequestContext crc, Manager<N> manager, Collection<O> parentIds) {
    try {
      Collection<N> entities = manager.readMany(HubAccess.fromContext(crc),
        parentIds.stream().map((id) -> UUID.fromString(String.valueOf(id))).collect(Collectors.toList()));
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      jsonapiPayload.setDataType(PayloadDataType.Many);
      for (N entity : entities) jsonapiPayload.addData(payloadFactory.toPayloadObject(entity));
      return response.ok(jsonapiPayload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Read many Entity of a Manager and return the JSON:API payload response

   @param crc      request context
   @param manager      of which to read many Entity
   @param parentId of Entity to read
   @param <N>      type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N> Response readMany(ContainerRequestContext crc, Manager<N> manager, String parentId) {
    if (Objects.isNull(parentId))
      return response.notAcceptable("parent id is required");

    return readMany(crc, manager, ImmutableList.of(parentId));
  }

  /**
   Update one Entity via a Manager given a JSON:API payload request

   @param crc            request context
   @param manager            via which to read one Entity
   @param id             of Entity to read
   @param jsonapiPayload of data to update
   @param <N>            type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N> Response update(ContainerRequestContext crc, Manager<N> manager, String id, JsonapiPayload jsonapiPayload) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      N updated = manager.update(hubAccess, UUID.fromString(id), payloadFactory.consume(manager.readOne(hubAccess, UUID.fromString(id)), jsonapiPayload));
      return response.ok(new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(updated)));

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }

  /**
   Update many Entities via a Manager given a JSON:API payload request

   @param crc            request context
   @param manager            via which to read one Entity
   @param jsonapiPayload of data to update, type:many
   @param <N>            type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N> Response updateMany(ContainerRequestContext crc, Manager<N> manager, JsonapiPayload jsonapiPayload) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      var result = new JsonapiPayload().setDataType(PayloadDataType.Many);
      for (var toUpdate : jsonapiPayload.getDataMany()) {
        N updated = payloadFactory.consume(manager.readOne(hubAccess, UUID.fromString(toUpdate.getId())), toUpdate);
        manager.update(hubAccess, UUID.fromString(toUpdate.getId()), updated);
        result.addData(payloadFactory.toPayloadObject(updated));
      }
      return response.ok(result);

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }

  /**
   Delete one Entity via a Manager

   @param crc request context
   @param manager via which to delete one Entity
   @param id  of Entity to delete
   @param <N> type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N> Response delete(ContainerRequestContext crc, Manager<N> manager, String id) {
    try {
      manager.destroy(HubAccess.fromContext(crc), UUID.fromString(id));
      return response.noContent();

    } catch (Exception e) {
      return response.failure(e);
    }
  }
}
