// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.dao.DAOException;
import io.xj.hub.dao.TemplatePublicationDAO;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.TemplatePublication;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.*;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 TemplatePublications
 */
@Path("api/1")
public class TemplatePublicationEndpoint extends HubJsonapiEndpoint<TemplatePublication> {
  private final TemplatePublicationDAO dao;

  /**
   Constructor
   */
  @Inject
  public TemplatePublicationEndpoint(
    TemplatePublicationDAO dao,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.dao = dao;
  }

  /**
   Get all templatePublications.

   @param templateId to get templatePublications for
   @return set of all templatePublications
   */
  @GET
  @Path("templates/{templateId}/publication")
  @RolesAllowed(ARTIST)
  public Response readManyForTemplate(
    @Context ContainerRequestContext crc,
    @PathParam("templateId") String templateId
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<TemplatePublication> templatePublications;

      // how we source templatePublications depends on the query parameters
      templatePublications = dao().readMany(hubAccess, ImmutableList.of(UUID.fromString(templateId)));

      // add templatePublications as plural data in payload
      for (TemplatePublication templatePublication : templatePublications)
        jsonapiPayload.addData(payloadFactory.toPayloadObject(templatePublication));

      return response.ok(jsonapiPayload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new templatePublication

   @param jsonapiPayload with which to update TemplatePublication record.
   @return Response
   */
  @POST
  @Path("template-publications")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {

    try {
      TemplatePublication templatePublication = payloadFactory.consume(dao().newInstance(), jsonapiPayload);
      TemplatePublication created;
      created = dao().create(
        HubAccess.fromContext(crc),
        templatePublication);

      return response.create(new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(created)));

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private TemplatePublicationDAO dao() {
    return dao;
  }
}
