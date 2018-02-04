// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.link_chord;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.LinkChordDAO;
import io.xj.core.model.link_chord.LinkChord;
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
import java.util.Objects;

/**
 LinkChords
 */
@Path("link-chords")
public class LinkChordIndexResource extends HubResource {
  private final LinkChordDAO linkChordDAO = injector.getInstance(LinkChordDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("linkId")
  String linkId;

  /**
   Get all linkChords.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(linkId) || linkId.isEmpty()) {
      return response.notAcceptable("Link id is required");
    }

    try {
      return response.readMany(
        LinkChord.KEY_MANY,
        linkChordDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(linkId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
