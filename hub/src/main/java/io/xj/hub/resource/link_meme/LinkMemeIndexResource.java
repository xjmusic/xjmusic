// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.link_meme;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.LinkMemeDAO;
import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;

/**
 Link record
 */
@Path("link-memes")
public class LinkMemeIndexResource extends HubResource {
  private final LinkMemeDAO linkMemeDAO = injector.getInstance(LinkMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("linkId")
  String linkId;

  /**
   Get Memes in one link.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (null == linkId || linkId.isEmpty()) {
      return response.notAcceptable("Link id is required");
    }

    try {
      return response.readMany(
        LinkMeme.KEY_MANY,
        linkMemeDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(linkId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
