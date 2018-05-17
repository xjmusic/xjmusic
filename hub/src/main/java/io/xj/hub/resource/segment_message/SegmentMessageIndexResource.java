// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.segment_message;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentMessageDAO;
import io.xj.core.model.segment_message.SegmentMessage;
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
 SegmentMessages
 */
@Path("segment-messages")
public class SegmentMessageIndexResource extends HubResource {
  private final SegmentMessageDAO segmentMessageDAO = injector.getInstance(SegmentMessageDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("segmentId")
  String segmentId;

  /**
   Get all segmentMessages.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(segmentId) || segmentId.isEmpty()) {
      return response.notAcceptable("Segment id is required");
    }

    try {
      return response.readMany(
        SegmentMessage.KEY_MANY,
        segmentMessageDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(segmentId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
