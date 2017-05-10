// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.phase_chord;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.PhaseChordDAO;
import io.outright.xj.core.model.phase_chord.PhaseChord;
import io.outright.xj.core.model.phase_chord.PhaseChordWrapper;
import io.outright.xj.core.model.role.Role;

import org.jooq.types.ULong;

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

/**
 PhaseChords
 */
@Path("phase-chords")
public class PhaseChordIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final PhaseChordDAO DAO = injector.getInstance(PhaseChordDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("phaseId")
  String phaseId;

  /**
   Get all phaseChords.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (phaseId == null || phaseId.length() == 0) {
      return response.notAcceptable("Phase id is required");
    }

    try {
      return response.readMany(
        PhaseChord.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(phaseId)));

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
  @RolesAllowed({Role.ARTIST})
  public Response create(PhaseChordWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        PhaseChord.KEY_MANY,
        PhaseChord.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getPhaseChord()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
