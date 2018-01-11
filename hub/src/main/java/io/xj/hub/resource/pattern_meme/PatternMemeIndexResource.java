// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.pattern_meme;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.pattern_meme.PatternMemeWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.server.HttpResponseProvider;



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
import java.math.BigInteger;

/**
 Pattern record
 */
@Path("pattern-memes")
public class PatternMemeIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final PatternMemeDAO patternMemeDAO = injector.getInstance(PatternMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("patternId")
  String patternId;

  /**
   Get Memes in one pattern.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (null == patternId || patternId.isEmpty()) {
      return response.notAcceptable("Pattern id is required");
    }

    try {
      return response.readMany(
        PatternMeme.KEY_MANY,
        patternMemeDAO.readAll(
          Access.fromContext(crc),
          new BigInteger(patternId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new pattern meme

   @param data with which to update Pattern record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(PatternMemeWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        PatternMeme.KEY_MANY,
        PatternMeme.KEY_ONE,
        patternMemeDAO.create(
          Access.fromContext(crc),
          data.getPatternMeme()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
