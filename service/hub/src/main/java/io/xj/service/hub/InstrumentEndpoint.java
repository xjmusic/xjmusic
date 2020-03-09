// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.app.AppResource;
import io.xj.lib.core.dao.DAO;
import io.xj.lib.core.dao.InstrumentDAO;
import io.xj.lib.core.dao.InstrumentMemeDAO;
import io.xj.lib.core.model.Instrument;
import io.xj.lib.core.model.UserRoleType;
import io.xj.lib.core.payload.MediaType;
import io.xj.lib.core.payload.Payload;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 Instruments
 */
@Path("instruments")
public class InstrumentEndpoint extends AppResource {
  private InstrumentDAO dao;
  private InstrumentMemeDAO instrumentMemeDAO;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public InstrumentEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(InstrumentDAO.class);
    instrumentMemeDAO = injector.getInstance(InstrumentMemeDAO.class);
  }

  /**
   Get all instruments.

   @param accountId to get instruments for
   @param libraryId to get instruments for
   @param include   (optional) "memes" or null
   @return set of all instruments
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(
    @Context ContainerRequestContext crc,
    @QueryParam("accountId") String accountId,
    @QueryParam("libraryId") String libraryId,
    @QueryParam("include") String include
  ) {
    try {
      Access access = Access.fromContext(crc);
      Payload payload = new Payload();
      Collection<Instrument> instruments;

      // how we source instruments depends on the query parameters
      if (null != libraryId && !libraryId.isEmpty())
        instruments = dao().readMany(access, ImmutableList.of(UUID.fromString(libraryId)));
      else if (null != accountId && !accountId.isEmpty())
        instruments = dao().readAllInAccount(access, UUID.fromString(accountId));
      else
        instruments = dao().readAll(access);

      // add instruments as plural data in payload
      payload.setDataEntities(instruments);
      Set<UUID> instrumentIds = DAO.idsFrom(instruments);

      // if included, seek and add events to payload
      if (Objects.nonNull(include) && include.contains("memes"))
        instrumentMemeDAO.readMany(access, instrumentIds)
          .forEach(instrumentMeme -> payload.addIncluded(instrumentMeme.toPayloadObject()));

      return response.ok(payload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new instrument

   @param payload with which to update Instrument record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(Payload payload, @Context ContainerRequestContext crc, @QueryParam("cloneId") String cloneId) {

    try {
      Instrument instrument = dao().newInstance().consume(payload);
      Instrument created;
      if (Objects.nonNull(cloneId))
        created = dao().clone(
          Access.fromContext(crc),
          UUID.fromString(cloneId),
          instrument);
      else
        created = dao().create(
          Access.fromContext(crc),
          instrument);

      return response.create(new Payload().setDataEntity(created));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }


  /**
   Get one instrument.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one instrument

   @param payload with which to update Instrument record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(Payload payload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, payload);
  }

  /**
   Delete one instrument

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(UserRoleType.ADMIN)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private InstrumentDAO dao() {
    return dao;
  }
}
