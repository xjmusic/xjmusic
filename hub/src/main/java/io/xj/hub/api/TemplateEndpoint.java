// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.DAOException;
import io.xj.hub.dao.TemplateDAO;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.jsonapi.*;
import io.xj.lib.util.CSV;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 Templates
 */
@Path("api/1")
public class TemplateEndpoint extends HubJsonapiEndpoint {
  private final TemplateDAO dao;

  /**
   Constructor
   */
  @Inject
  public TemplateEndpoint(
    TemplateDAO dao,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory
  ) {
    super(response, payloadFactory);
    this.dao = dao;
  }

  /**
   Get all templates.

   @return application/json response.
   */
  @GET
  @Path("templates")
  @RolesAllowed(USER)
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("accountId") String accountId) {
    if (Objects.nonNull(accountId))
      return readMany(crc, dao(), accountId);
    else
      return readMany(crc, dao(), HubAccess.fromContext(crc).getAccountIds());
  }

  /**
   Create new template

   @param jsonapiPayload with which to update Template record.
   @return Response
   */
  @POST
  @Path("templates")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed({ADMIN, ENGINEER})
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), jsonapiPayload);
  }

  /**
   Get one template.

   @return application/json response.
   */
  @GET
  @Path("templates/{id}")
  @RolesAllowed(USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String identifier, @QueryParam("include") String include) {
    var access = HubAccess.fromContext(crc);

    @Nullable UUID uuid;
    try {
      uuid = UUID.fromString(identifier);
    } catch (Exception ignored) {
      uuid = null;
    }

    try {
      Template entity = Objects.isNull(uuid)
        ? dao.readOneByShipKey(access, identifier).orElseThrow(() -> new DAOException("not found"))
        : dao.readOne(access, uuid);
      uuid = entity.getId();

      JsonapiPayload jsonapiPayload = new JsonapiPayload();
      jsonapiPayload.setDataOne(payloadFactory.toPayloadObject(entity));

      // optionally specify a CSV of included types to read
      if (Objects.nonNull(include)) {
        List<JsonapiPayloadObject> list = new ArrayList<>();
        for (Object included : dao().readChildEntities(access, ImmutableList.of(uuid), CSV.split(include)))
          list.add(payloadFactory.toPayloadObject(included));
        jsonapiPayload.setIncluded(list);
      }

      return response.ok(jsonapiPayload);

    } catch (DAOException ignored) {
      return response.notFound(dao.newInstance().getClass(), identifier);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one template

   @param jsonapiPayload with which to update Template record.
   @return Response
   */
  @PATCH
  @Path("templates/{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed({ADMIN, ENGINEER})
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, jsonapiPayload);
  }

  /**
   Delete one template

   @return Response
   */
  @DELETE
  @Path("templates/{id}")
  @RolesAllowed({ADMIN, ENGINEER})
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get all templatePlaybacks.

   @return set of all templatePlaybacks
   */
  @GET
  @Path("templates/playing")
  @RolesAllowed(USER)
  public Response readAllPlaying(
    @Context ContainerRequestContext crc
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);

      // add templatePlaybacks as plural data in payload
      for (var template : dao().readAllPlaying(hubAccess))
        jsonapiPayload.addData(payloadFactory.toPayloadObject(template));

      return response.ok(jsonapiPayload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }


  /**
   Get DAO of injector

   @return DAO
   */
  private TemplateDAO dao() {
    return dao;
  }
}
