// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.segment_chord;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentChordDAO;
import io.xj.core.model.segment_chord.SegmentChord;
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
 SegmentChords
 */
@Path("segment-chords")
public class SegmentChordIndexResource extends HubResource {
  private final SegmentChordDAO segmentChordDAO = injector.getInstance(SegmentChordDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("segmentId")
  String segmentId;

  /**
   Get all segmentChords.

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
        SegmentChord.KEY_MANY,
        segmentChordDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(segmentId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
