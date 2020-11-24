// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.protobuf.MessageLite;
import com.typesafe.config.Config;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.DAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;

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
  protected final HttpResponseProvider response;
  protected final PayloadFactory payloadFactory;

  /**
   Constructor
   */
  @Inject
  public NexusEndpoint(
    HttpResponseProvider response,
    Config config,
    PayloadFactory payloadFactory
  ) {
    this.response = response;
    this.config = config;
    this.payloadFactory = payloadFactory;
  }

  /**
   Create one Entity via a DAO given a JSON:API payload request

   @param crc     request context
   @param dao     via which to of Entity
   @param payload of data to of Entity
   @param <N>     type of Entity
   @return HTTP response comprising JSON:API payload
   */
  public <N extends MessageLite> Response create(ContainerRequestContext crc, DAO<N> dao, Payload payload) {
    try {
      HubClientAccess hubClientAccess = HubClientAccess.fromContext(crc);
      N createdEntity = dao.create(hubClientAccess, payloadFactory.consume(dao.newInstance(), payload));

      Payload responseData = new Payload();
      responseData.setDataOne(payloadFactory.toPayloadObject(createdEntity));
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
      Payload payload = new Payload();
      payload.setDataOne(payloadFactory.toPayloadObject(entity));
      return response.ok(payload);

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
      Payload payload = new Payload();
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
  public <N extends MessageLite> Response readMany(ContainerRequestContext crc, DAO<N> dao, String parentId) {
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
  public <N extends MessageLite> Response update(ContainerRequestContext crc, DAO<N> dao, String id, Payload payload) {
    try {
      HubClientAccess hubClientAccess = HubClientAccess.fromContext(crc);
      N current = dao.readOne(hubClientAccess, id);
      payloadFactory.consume(current, payload);
      dao.update(hubClientAccess, id, current);
      return response.ok(new Payload().setDataOne(payloadFactory.toPayloadObject(current)));

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
