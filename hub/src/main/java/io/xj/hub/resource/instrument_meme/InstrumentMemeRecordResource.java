// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.instrument_meme;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.model.instrument_meme.InstrumentMeme;



import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;

/**
 Instrument record
 */
@Path("instrument-memes/{id}")
public class InstrumentMemeRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final InstrumentMemeDAO instrumentMemeDAO = injector.getInstance(InstrumentMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one InstrumentMeme by instrumentId and memeId

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        InstrumentMeme.KEY_ONE,
        instrumentMemeDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Delete one InstrumentMeme by instrumentId and memeId

   @return application/json response.
   */
  @DELETE
  @RolesAllowed(UserRoleType.ARTIST)
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      instrumentMemeDAO.delete(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();
    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
