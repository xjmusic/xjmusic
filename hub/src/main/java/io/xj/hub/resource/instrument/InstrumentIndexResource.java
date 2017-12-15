// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.instrument;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Exposure;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.model.Entity;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.transport.JSON;



import com.google.inject.Guice;
import com.google.inject.Injector;

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
import java.math.BigInteger;

/**
 Instruments
 */
@Path("instruments")
public class InstrumentIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final InstrumentDAO instrumentDAO = injector.getInstance(InstrumentDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("accountId")
  String accountId;

  @QueryParam("libraryId")
  String libraryId;

  /**
   Get all instruments.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (null != libraryId && !libraryId.isEmpty()) {
      return readAllInLibrary(Access.fromContext(crc));
    } else if (null != accountId && !accountId.isEmpty()) {
      return readAllInAccount(Access.fromContext(crc));
    } else {
      return response.notAcceptable("Either Account or Library id is required");
    }
  }

  private Response readAllInAccount(Access access) {
    try {
      return response.readMany(
        Instrument.KEY_MANY,
        instrumentDAO.readAllInAccount(
          access,
          new BigInteger(accountId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAllInLibrary(Access access) {
    try {
      return response.readMany(
        Instrument.KEY_MANY,
        instrumentDAO.readAllInLibrary(
          access,
          new BigInteger(libraryId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new instrument

   @param data with which to update Instrument record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(InstrumentWrapper data, @Context ContainerRequestContext crc) {
    Access access = Access.fromContext(crc);
    try {
      JSONObject newEntity = JSON.objectFrom(instrumentDAO.create(access, data.getInstrument()));
      return Response
        .created(Exposure.apiURI(Instrument.KEY_MANY + "/" + newEntity.get(Entity.KEY_ID)))
        .entity(JSON.wrap(Instrument.KEY_ONE, newEntity).toString())
        .build();

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
