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
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Link record
 */
@Path("link-memes/{id}")
public class LinkMemeRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final LinkMemeDAO DAO = injector.getInstance(LinkMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one LinkMeme by linkId and memeId

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        LinkMeme.KEY_ONE,
        DAO.readOne(
          Access.fromContext(crc),
          ULong.valueOf(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
