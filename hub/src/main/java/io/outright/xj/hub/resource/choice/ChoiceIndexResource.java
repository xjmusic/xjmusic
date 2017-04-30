// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.choice;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.ChoiceDAO;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Choices
 */
@Path("choices")
public class ChoiceIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static Logger log = LoggerFactory.getLogger(ChoiceIndexResource.class);
  private final ChoiceDAO choiceDAO = injector.getInstance(ChoiceDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("linkId")
  String linkId;

  /**
   Get all choices.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    if (linkId == null || linkId.length() == 0) {
      return httpResponseProvider.notAcceptable("Link id is required");
    }

    try {
      JSONArray result = choiceDAO.readAllIn(access, ULong.valueOf(linkId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(Choice.KEY_MANY, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return Response.noContent().build();
      }

    } catch (Exception e) {
      log.error("Exception", e);
      return httpResponseProvider.failure(e);
    }
  }

}
