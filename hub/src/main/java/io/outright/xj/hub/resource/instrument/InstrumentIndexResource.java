// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.instrument;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.InstrumentDAO;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.model.instrument.InstrumentWrapper;
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
 * Instruments
 */
@Path("instruments")
public class InstrumentIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  //  private static Logger log = LoggerFactory.getLogger(InstrumentIndexResource.class);
  private final InstrumentDAO instrumentDAO = injector.getInstance(InstrumentDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("accountId")
  String accountId;

  @QueryParam("libraryId")
  String libraryId;

  /**
   * Get all instruments.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);
    if (libraryId != null && libraryId.length() > 0) {
      return readAllInLibrary(access);
    } else if (accountId != null && accountId.length() > 0) {
      return readAllInAccount(access);
    } else {
      return httpResponseProvider.notAcceptable("Either Account or Library id is required");
    }
  }

  private Response readAllInAccount(AccessControl access) {
    try {
      JSONArray result = instrumentDAO.readAllInAccount(access, ULong.valueOf(accountId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(Instrument.KEY_MANY, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return Response.noContent().build();
      }

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

  private Response readAllInLibrary(AccessControl access) {
    try {
      JSONArray result = instrumentDAO.readAllInLibrary(access, ULong.valueOf(libraryId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(Instrument.KEY_MANY, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return Response.noContent().build();
      }

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

  /**
   * Create new instrument
   *
   * @param data with which to update Instrument record.
   * @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(InstrumentWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      JSONObject newEntity = instrumentDAO.create(access, data);
      return Response
        .created(Exposure.apiURI(Instrument.KEY_MANY + "/" + newEntity.get(Entity.KEY_ID)))
        .entity(JSON.wrap(Instrument.KEY_ONE, newEntity).toString())
        .build();

    } catch (Exception e) {
      return httpResponseProvider.failureToCreate(e);
    }
  }

}
