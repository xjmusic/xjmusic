// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.protobuf.MessageLite;
import com.typesafe.config.Config;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.dao.DAO;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.hub_client.client.HubClientAccess;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 A JAX-RS resource
 */
public class NexusEndpoint {
  public static final String ADMIN = "Admin";
  public static final String ARTIST = "Artist";
  public static final String BANNED = "Banned";
  public static final String ENGINEER = "Engineer";
  public static final String INTERNAL = "Internal";
  public static final String USER = "User";
  protected final Config config;
  protected final JsonapiHttpResponseProvider response;
  protected final JsonapiPayloadFactory jsonapiPayloadFactory;

  /**
   Constructor
   */
  @Inject
  public NexusEndpoint(
    JsonapiHttpResponseProvider response,
    Config config,
    JsonapiPayloadFactory jsonapiPayloadFactory
  ) {
    this.response = response;
    this.config = config;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
  }

  /**
   Create one Entity via a DAO given a JSON:API payload request

   @param crc     request context
   @param dao     via which to of Entity
   @param jsonapiPayload of data to of Entity
   @param <N>     type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends MessageLite> Response create(ContainerRequestContext crc, DAO<N> dao, JsonapiPayload jsonapiPayload) {
    try {
      HubClientAccess hubClientAccess = HubClientAccess.fromContext(crc);
      N createdEntity = dao.create(hubClientAccess, jsonapiPayloadFactory.consume(dao.newInstance(), jsonapiPayload));

      JsonapiPayload responseData = new JsonapiPayload();
      responseData.setDataOne(jsonapiPayloadFactory.toPayloadObject(createdEntity));
      return response.create(responseData);

    } catch (Exception e) {
      return response.notAcceptable(e);
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
  public <N extends MessageLite> Response readOne(ContainerRequestContext crc, DAO<N> dao, Object id) {
    try {
      Object entity = dao.readOne(HubClientAccess.fromContext(crc), String.valueOf(id));
      JsonapiPayload jsonapiPayload = new JsonapiPayload();
      jsonapiPayload.setDataOne(jsonapiPayloadFactory.toPayloadObject(entity));
      return response.ok(jsonapiPayload);

    } catch (DAOExistenceException | DAOFatalException | DAOPrivilegeException ignored) {
      return response.notFound(dao.newInstance().getClass(), String.valueOf(id));

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
  public <N extends MessageLite, O> Response readMany(ContainerRequestContext crc, DAO<N> dao, Collection<O> parentIds) {
    try {
      Collection<N> entities = dao.readMany(HubClientAccess.fromContext(crc), parentIds.stream().map((Function<Object, String>) String::valueOf).collect(Collectors.toList()));
      JsonapiPayload jsonapiPayload = new JsonapiPayload();
      jsonapiPayload.setDataType(PayloadDataType.Many);
      for (N entity : entities) jsonapiPayload.addData(jsonapiPayloadFactory.toPayloadObject(entity));
      return response.ok(jsonapiPayload);

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
  public <N extends MessageLite> Response readMany(ContainerRequestContext crc, DAO<N> dao, String parentId) {
    if (Objects.isNull(parentId))
      return response.notAcceptable("parent id is required.");

    return readMany(crc, dao, ImmutableList.of(parentId));
  }

  /**
   Update one Entity via a DAO given a JSON:API payload request

   @param crc     request context
   @param dao     via which to read one Entity
   @param id      of Entity to read
   @param jsonapiPayload of data to update
   @param <N>     type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends MessageLite> Response update(ContainerRequestContext crc, DAO<N> dao, String id, JsonapiPayload jsonapiPayload) {
    try {
      HubClientAccess hubClientAccess = HubClientAccess.fromContext(crc);
      N current = dao.readOne(hubClientAccess, id);
      var updated = dao.update(hubClientAccess, id, jsonapiPayloadFactory.consume(current, jsonapiPayload));
      return response.ok(new JsonapiPayload().setDataOne(jsonapiPayloadFactory.toPayloadObject(updated)));

    } catch (Exception e) {
      return response.notAcceptable(e);
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
  public <N extends MessageLite> Response delete(ContainerRequestContext crc, DAO<N> dao, String id) {
    try {
      dao.destroy(HubClientAccess.fromContext(crc), id);
      return response.noContent();

    } catch (Exception e) {
      return response.failure(e);
    }
  }
}
