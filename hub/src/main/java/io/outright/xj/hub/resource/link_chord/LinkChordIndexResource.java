// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.link_chord;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.LinkChordDAO;
import io.outright.xj.core.model.link_chord.LinkChord;
import io.outright.xj.core.model.role.Role;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 LinkChords
 */
@Path("link-chords")
public class LinkChordIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final LinkChordDAO DAO = injector.getInstance(LinkChordDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("linkId")
  String linkId;

  /**
   Get all linkChords.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (linkId == null || linkId.length() == 0) {
      return response.notAcceptable("Link id is required");
    }

    try {
      return response.readMany(
        LinkChord.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(linkId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
