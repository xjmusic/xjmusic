// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.Chain;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubClientException;
import io.xj.service.nexus.NexusEndpoint;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;

import javax.annotation.Nullable;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.UUID;

/**
 Chains
 */
@Path("chains")
public class ChainEndpoint extends NexusEndpoint {
  private final ChainDAO dao;
  private final EntityFactory entityFactory;

  /**
   Constructor
   */
  @Inject
  public ChainEndpoint(
    ChainDAO dao,
    HttpResponseProvider response,
    Config config,
    PayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(response, config, payloadFactory);
    this.dao = dao;
    this.entityFactory = entityFactory;
  }

  /**
   Get all chains.

   @return application/json response.
   */
  @GET
  @RolesAllowed(USER)
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("accountId") String accountId) {
    try {
      HubClientAccess access = HubClientAccess.fromContext(crc);

      // Account ID required
      if (Objects.isNull(accountId))
        return response.notAcceptable("Account ID is required!");

      // read chain either by uuid (private) or string (public embed key) identifier
      return response.ok(payloadFactory.newPayload().setDataMany(
        payloadFactory.toPayloadObjects(dao.readMany(access, ImmutableList.of(accountId)))));

    } catch (DAOExistenceException e) {
      return response.notFound(Account.class, accountId);

    } catch (DAOPrivilegeException e) {
      return response.unauthorized(Account.class, accountId, e);

    } catch (JsonApiException | DAOFatalException e) {
      return response.failure(e);
    }
  }

  /**
   Create new chain
   -or-
   [#160299309] Engineer wants a *revived* action for a live production chain

   @param payload with which to update Chain record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(Payload payload, @Context ContainerRequestContext crc, @QueryParam("reviveId") String reviveId) {
    try {
      HubClientAccess access = HubClientAccess.fromContext(crc);
      // if present, we will revive a prior chain, else create a new one
      Chain chain = Objects.nonNull(reviveId) && !reviveId.isEmpty() ?
        dao.revive(access, reviveId, String.format("Requested by User[%s]", access.getUserId())) :
        dao.create(access, payloadFactory.consume(entityFactory.getInstance(Chain.class), payload));

      // create either a new chain, or a chain revived from an existing prior chain
      return response.create(payloadFactory.newPayload().setDataOne(payloadFactory.toPayloadObject(chain)));

    } catch (DAOPrivilegeException e) {
      return Objects.nonNull(reviveId) ?
        response.unauthorized(Chain.class, reviveId, e) :
        response.unauthorized();

    } catch (DAOExistenceException e) {
      return response.notFound(Chain.class, reviveId);

    } catch (DAOValidationException | HubClientException | JsonApiException | DAOFatalException | EntityException e) {
      return response.notAcceptable(e);
    }
  }

  /**
   Get one chain.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @PermitAll
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String identifier) {
    try {
      HubClientAccess access = HubClientAccess.fromContext(crc);

      // identifier required
      if (Objects.isNull(identifier) || identifier.isEmpty())
        return response.notAcceptable("Chain id is required");

      // will only have value if this can parse a uuid from string
      // otherwise, ignore the exception on attempt and store a null value for uuid
      @Nullable String uuidId;
      try {
        uuidId = UUID.fromString(identifier).toString();
      } catch (Exception ignored) {
        uuidId = null;
      }

      // read chain either by uuid (private) or string (public embed key) identifier
      return response.ok(payloadFactory.newPayload().setDataOne(
        payloadFactory.toPayloadObject(
          Objects.nonNull(uuidId) ?
            dao.readOne(access, uuidId) :
            dao.readOneByEmbedKey(access, identifier))));

    } catch (DAOPrivilegeException e) {
      return response.unauthorized(Chain.class, identifier, e);

    } catch (DAOExistenceException e) {
      return response.notFound(Chain.class, identifier);

    } catch (JsonApiException | DAOFatalException e) {
      return response.failure(e);
    }
  }

  /**
   Update one chain

   @param payload with which to update Chain record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response update(Payload payload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    try {
      HubClientAccess access = HubClientAccess.fromContext(crc);

      // Chain ID required
      if (Objects.isNull(id))
        return response.notAcceptable("Chain ID is required!");

      // Consume input payload onto existing Chain record, then update
      Chain chain = payloadFactory.consume(dao.readOne(access, id), payload);
      dao.update(access, id, chain);
      return response.ok(payloadFactory.newPayload().setDataOne(payloadFactory.toPayloadObject(chain)));

    } catch (DAOValidationException e) {
      return response.notAcceptable(e.getMessage());

    } catch (DAOPrivilegeException e) {
      return response.unauthorized(Chain.class, id, e);

    } catch (DAOExistenceException e) {
      return response.notFound(Chain.class, id);

    } catch (JsonApiException | DAOFatalException e) {
      return response.notAcceptable(e);
    }
  }

  /**
   Delete one chain

   @return Response
   */
  @DELETE
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    try {
      HubClientAccess access = HubClientAccess.fromContext(crc);

      // Chain ID required
      if (Objects.isNull(id))
        return response.notAcceptable("Chain ID is required!");

      // Destroy chain
      dao.destroy(access, id);
      return response.noContent();

    } catch (DAOPrivilegeException e) {
      return response.unauthorized(Chain.class, id, e);

    } catch (DAOExistenceException e) {
      return response.notFound(Chain.class, id);

    } catch (DAOFatalException e) {
      return response.notAcceptable(e);
    }
  }


}
