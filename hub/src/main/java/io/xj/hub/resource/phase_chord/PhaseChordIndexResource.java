// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.phase_chord;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.PhaseChordDAO;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.model.phase_chord.PhaseChordWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;

import com.google.common.collect.ImmutableList;
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
import java.util.Objects;

/**
 PhaseChords
 */
@Path("phase-chords")
public class PhaseChordIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final PhaseChordDAO phaseChordDAO = injector.getInstance(PhaseChordDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("phaseId")
  String phaseId;

  /**
   Get all phaseChords.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(phaseId) || phaseId.isEmpty()) {
      return response.notAcceptable("Phase id is required");
    }

    try {
      return response.readMany(
        PhaseChord.KEY_MANY,
        phaseChordDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(phaseId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new phaseChord

   @param data with which to update PhaseChord record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(PhaseChordWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        PhaseChord.KEY_MANY,
        PhaseChord.KEY_ONE,
        phaseChordDAO.create(
          Access.fromContext(crc),
          data.getPhaseChord()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
