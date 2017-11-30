// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.link_meme;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.LinkMemeDAO;
import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.model.role.Role;

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
 Link record
 */
@Path("link-memes")
public class LinkMemeIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final LinkMemeDAO DAO = injector.getInstance(LinkMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("linkId")
  String linkId;

  /**
   Get Memes in one link.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (linkId == null || linkId.length() == 0) {
      return response.notAcceptable("Link id is required");
    }

    try {
      return response.readMany(
        LinkMeme.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(linkId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
