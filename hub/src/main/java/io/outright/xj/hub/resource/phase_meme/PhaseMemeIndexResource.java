// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.phase_meme;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.PhaseMemeDAO;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.phase_meme.PhaseMeme;
import io.outright.xj.core.model.phase_meme.PhaseMemeWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;

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
 * Phase record
 */
@Path("phase-memes")
public class PhaseMemeIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  //  private static Logger log = LoggerFactory.getLogger(PhaseMemeIndexResource.class);
  private final PhaseMemeDAO phaseMemeDAO = injector.getInstance(PhaseMemeDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("phase")
  String phaseId;

  /**
   * Get Memes in one phase.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    if (phaseId == null || phaseId.length() == 0) {
      return httpResponseProvider.notAcceptable("Phase id is required");
    }

    try {
      JSONArray result = phaseMemeDAO.readAllIn(access, ULong.valueOf(phaseId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(PhaseMeme.KEY_MANY, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return httpResponseProvider.notFound("Phase Meme");
      }

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

  /**
   * Create new phase meme
   *
   * @param data with which to update Phase record.
   * @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(PhaseMemeWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      JSONObject result = phaseMemeDAO.create(access, data);
      return Response
        .created(Exposure.apiURI(PhaseMeme.KEY_MANY + "/" + result.get(Entity.KEY_ID)))
        .entity(JSON.wrap(PhaseMeme.KEY_ONE, result).toString())
        .build();

    } catch (Exception e) {
      return httpResponseProvider.failureToCreate(e);
    }
  }
}
