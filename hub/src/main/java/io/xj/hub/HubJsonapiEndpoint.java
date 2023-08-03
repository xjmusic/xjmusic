// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub;

import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.Manager;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.PayloadDataType;
import org.springframework.http.ResponseEntity;

import org.jetbrains.annotations.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A JAX-RS resource
 */
public class HubJsonapiEndpoint extends HubPersistenceServiceImpl {
  public static final String ADMIN = "Admin";
  public static final String ARTIST = "Artist";
  //  public static final String BANNED = "Banned";
  public static final String ENGINEER = "Engineer";
  public static final String INTERNAL = "Internal";
  public static final String USER = "User";
  protected final JsonapiResponseProvider responseProvider;
  protected final JsonapiPayloadFactory payloadFactory;

  /**
   * Constructor
   */
  public HubJsonapiEndpoint(
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider responseProvider,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(entityFactory, sqlStoreProvider);
    this.responseProvider = responseProvider;
    this.payloadFactory = payloadFactory;
  }

  /**
   * Create one Entity via a Manager given a JSON:API payload request
   *
   * @param req            request
   * @param manager        via which to of Entity
   * @param jsonapiPayload of data to of Entity
   * @param <N>            type of Entity
   * @return HTTP response comprising JSON:API payload
   */
  public <N> ResponseEntity<JsonapiPayload> create(HttpServletRequest req, Manager<N> manager, JsonapiPayload jsonapiPayload) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
      N createdEntity = manager.create(access, payloadFactory.consume(manager.newInstance(), jsonapiPayload));

      JsonapiPayload responseData = new JsonapiPayload();
      responseData.setDataOne(payloadFactory.toPayloadObject(createdEntity));
      return responseProvider.create(responseData);

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Read one Entity of a Manager and return the JSON:API payload response
   *
   * @param req     request
   * @param manager of which to read one Entity
   * @param id      of Entity to read
   * @param <N>     type of Entity
   * @return HTTP response comprising JSON:API payload
   */
  public <N> ResponseEntity<JsonapiPayload> readOne(HttpServletRequest req, Manager<N> manager, Object id) {
    try {
      @Nullable Object entity = manager.readOne(HubAccess.fromRequest(req), UUID.fromString(String.valueOf(id)));
      if (Objects.isNull(entity))
        return responseProvider.notFound(manager.newInstance().getClass(), UUID.fromString(String.valueOf(id)));
      JsonapiPayload jsonapiPayload = new JsonapiPayload();
      jsonapiPayload.setDataOne(payloadFactory.toPayloadObject(entity));
      return responseProvider.ok(jsonapiPayload);

    } catch (ManagerException ignored) {
      return responseProvider.notFound(manager.newInstance().getClass(), UUID.fromString(String.valueOf(id)));

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Read many Entity of a Manager and return the JSON:API payload response
   *
   * @param req       request
   * @param manager   of which to read many Entity
   * @param parentIds of Entity to read
   * @param <N>       type of Entity
   * @return HTTP response comprising JSON:API payload
   */
  public <N, O> ResponseEntity<JsonapiPayload> readMany(HttpServletRequest req, Manager<N> manager, Collection<O> parentIds) {
    try {
      Collection<N> entities = manager.readMany(HubAccess.fromRequest(req),
        parentIds.stream().map((id) -> UUID.fromString(String.valueOf(id))).collect(Collectors.toList()));
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      jsonapiPayload.setDataType(PayloadDataType.Many);
      for (N entity : entities) jsonapiPayload.addData(payloadFactory.toPayloadObject(entity));
      return responseProvider.ok(jsonapiPayload);

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Read many Entity of a Manager and return the JSON:API payload response
   *
   * @param req      request
   * @param manager  of which to read many Entity
   * @param parentId of Entity to read
   * @param <N>      type of Entity
   * @return HTTP response comprising JSON:API payload
   */
  public <N> ResponseEntity<JsonapiPayload> readMany(HttpServletRequest req, Manager<N> manager, UUID parentId) {
    if (Objects.isNull(parentId)) return responseProvider.notAcceptable("parent id is required");

    return readMany(req, manager, List.of(parentId));
  }

  /**
   * Update one Entity via a Manager given a JSON:API payload request
   *
   * @param req            request
   * @param manager        via which to read one Entity
   * @param id             of Entity to read
   * @param jsonapiPayload of data to update
   * @param <N>            type of Entity
   * @return HTTP response comprising JSON:API payload
   */
  public <N> ResponseEntity<JsonapiPayload> update(HttpServletRequest req, Manager<N> manager, UUID id, JsonapiPayload jsonapiPayload) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
      N updated = manager.update(access, id, payloadFactory.consume(manager.readOne(access, id), jsonapiPayload));
      return responseProvider.ok(new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(updated)));

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Update many Entities via a Manager given a JSON:API payload request
   *
   * @param req            request
   * @param manager        via which to read one Entity
   * @param jsonapiPayload of data to update, type:many
   * @param <N>            type of Entity
   * @return HTTP response comprising JSON:API payload
   */
  public <N> ResponseEntity<JsonapiPayload> updateMany(HttpServletRequest req, Manager<N> manager, JsonapiPayload jsonapiPayload) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
      var result = new JsonapiPayload().setDataType(PayloadDataType.Many);
      for (var toUpdate : jsonapiPayload.getDataMany()) {
        N updated = payloadFactory.consume(manager.readOne(access, UUID.fromString(toUpdate.getId())), toUpdate);
        manager.update(access, UUID.fromString(toUpdate.getId()), updated);
        result.addData(payloadFactory.toPayloadObject(updated));
      }
      return responseProvider.ok(result);

    } catch (Exception e) {
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Delete one Entity via a Manager
   *
   * @param req     request
   * @param manager via which to delete one Entity
   * @param id      of Entity to delete
   * @param <N>     type of Entity
   * @return HTTP response comprising JSON:API payload
   */
  public <N> ResponseEntity<JsonapiPayload> delete(HttpServletRequest req, Manager<N> manager, UUID id) {
    try {
      manager.destroy(HubAccess.fromRequest(req), id);
      return responseProvider.deletedOk();

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }
}
