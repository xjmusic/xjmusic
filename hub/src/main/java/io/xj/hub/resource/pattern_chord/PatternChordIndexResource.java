// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.pattern_chord;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternChordDAO;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.pattern_chord.PatternChordWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
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
import java.util.Objects;

/**
 PatternChords
 */
@Path("pattern-chords")
public class PatternChordIndexResource extends HubResource {
  private final PatternChordDAO patternChordDAO = injector.getInstance(PatternChordDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("patternId")
  String patternId;

  /**
   Get all patternChords.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(patternId) || patternId.isEmpty()) {
      return response.notAcceptable("Pattern id is required");
    }

    try {
      return response.readMany(
        PatternChord.KEY_MANY,
        patternChordDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(patternId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new patternChord

   @param data with which to update PatternChord record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(PatternChordWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        PatternChord.KEY_MANY,
        PatternChord.KEY_ONE,
        patternChordDAO.create(
          Access.fromContext(crc),
          data.getPatternChord()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
