// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.idea;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.IdeaDAO;
import io.xj.core.model.idea.Idea;
import io.xj.core.model.idea.IdeaWrapper;
import io.xj.core.model.role.Role;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Ideas
 */
@Path("ideas")
public class IdeaIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final IdeaDAO DAO = injector.getInstance(IdeaDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("accountId")
  String accountId;

  @QueryParam("libraryId")
  String libraryId;

  /**
   Get all ideas.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (libraryId != null && libraryId.length() > 0) {
      return readAllInLibrary(Access.fromContext(crc));
    } else if (accountId != null && accountId.length() > 0) {
      return readAllInAccount(Access.fromContext(crc));
    } else {
      return response.notAcceptable("Either Account or Library id is required");
    }
  }

  private Response readAllInAccount(Access access) {
    try {
      return response.readMany(
        Idea.KEY_MANY,
        DAO.readAllInAccount(
          access,
          ULong.valueOf(accountId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAllInLibrary(Access access) {
    try {
      return response.readMany(
        Idea.KEY_MANY,
        DAO.readAllInLibrary(
          access,
          ULong.valueOf(libraryId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new idea

   @param data with which to update Idea record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(IdeaWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        Idea.KEY_MANY,
        Idea.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getIdea()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
