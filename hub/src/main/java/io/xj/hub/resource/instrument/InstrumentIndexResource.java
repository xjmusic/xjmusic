// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.instrument;

import io.xj.core.CoreModule;
import io.xj.core.app.access.impl.Access;
import io.xj.core.app.config.Exposure;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.server.HttpResponseProvider;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.model.Entity;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentWrapper;
import io.xj.core.model.role.Role;
import io.xj.core.transport.JSON;

import org.jooq.types.ULong;

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

/**
 Instruments
 */
@Path("instruments")
public class InstrumentIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final InstrumentDAO DAO = injector.getInstance(InstrumentDAO.class);
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
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (libraryId != null && libraryId.length() > 0) {
      return readAllInLibrary(Access.fromContext(crc));
    } else if (accountId != null && accountId.length() > 0) {
      return readAllInAccount(Access.fromContext(crc));
    } else {
      return response.notAcceptable("Either Account or Library id is required");
    }
  }

  private Response readAllInAccount(Access access) {
    try {
      return response.readMany(
        Instrument.KEY_MANY,
        DAO.readAllInAccount(
          access,
          ULong.valueOf(accountId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAllInLibrary(Access access) {
    try {
      return response.readMany(
        Instrument.KEY_MANY,
        DAO.readAllInLibrary(
          access,
          ULong.valueOf(libraryId)));

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
  @RolesAllowed({Role.ARTIST})
  public Response create(InstrumentWrapper data, @Context ContainerRequestContext crc) {
    Access access = Access.fromContext(crc);
    try {
      JSONObject newEntity = JSON.objectFromRecord(DAO.create(access, data.getInstrument()));
      if (newEntity != null) {
        return Response
          .created(Exposure.apiURI(Instrument.KEY_MANY + "/" + newEntity.get(Entity.KEY_ID)))
          .entity(JSON.wrap(Instrument.KEY_ONE, newEntity).toString())
          .build();
      } else {
        return response.failureToCreate(new BusinessException("Could not create record"));
      }

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
