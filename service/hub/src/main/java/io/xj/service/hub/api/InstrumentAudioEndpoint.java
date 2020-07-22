// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.xj.lib.entity.Entity;
import io.xj.lib.jsonapi.MediaType;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.dao.InstrumentAudioChordDAO;
import io.xj.service.hub.dao.InstrumentAudioDAO;
import io.xj.service.hub.dao.InstrumentAudioEventDAO;
import io.xj.service.hub.entity.InstrumentAudio;
import io.xj.service.hub.entity.InstrumentAudioChord;
import io.xj.service.hub.entity.InstrumentAudioEvent;
import io.xj.service.hub.entity.UserRoleType;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 InstrumentAudio endpoint
 */
@Path("instrument-audios")
public class InstrumentAudioEndpoint extends HubEndpoint {
  private final InstrumentAudioEventDAO instrumentAudioEventDAO;
  private final InstrumentAudioChordDAO instrumentAudioChordDAO;
  private InstrumentAudioDAO dao;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public InstrumentAudioEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(InstrumentAudioDAO.class);
    this.instrumentAudioEventDAO = injector.getInstance(InstrumentAudioEventDAO.class);
    this.instrumentAudioChordDAO = injector.getInstance(InstrumentAudioChordDAO.class);
  }

  /**
   Create new instrumentAudio binding

   @param payload with which to of InstrumentAudio Binding
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed({UserRoleType.ARTIST})
  public Response create(Payload payload, @Context ContainerRequestContext crc, @QueryParam("cloneId") String cloneId) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      InstrumentAudio instrumentAudio = payloadFactory.consume(dao().newInstance(), payload);
      InstrumentAudio created;
      if (Objects.nonNull(cloneId))
        created = dao().clone(hubAccess, UUID.fromString(cloneId), instrumentAudio);
      else
        created = dao().create(hubAccess, instrumentAudio);

      return response.create(new Payload().setDataOne(payloadFactory.toPayloadObject(created)));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

  /**
   Generate an Upload policy to upload the corresponding file to 3rd-party storage (e.g. Amazon S3)

   @return application/json response.
   */
  @GET
  @Path("{id}/upload")
  @RolesAllowed(UserRoleType.ARTIST)
  public Response uploadOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    try {
      Map<String, String> result = dao().authorizeUpload(HubAccess.fromContext(crc), UUID.fromString(id));
      if (null != result) {
        return Response
          .accepted(payloadFactory.serialize(result))
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return response.notFound(dao.newInstance().setId(UUID.fromString(id)));
      }


    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Get one InstrumentAudio by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed({UserRoleType.ARTIST})
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Get Bindings in one instrumentAudio.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ARTIST})
  public Response readMany(@Context ContainerRequestContext crc, @QueryParam("instrumentId") String instrumentId, @QueryParam("include") String include) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      Payload payload = new Payload().setDataType(PayloadDataType.Many);
      Collection<InstrumentAudio> instrumentAudios = dao.readMany(HubAccess.fromContext(crc), ImmutableList.of(UUID.fromString(instrumentId)));
      Set<UUID> instrumentAudioIds = Entity.idsOf(instrumentAudios);

      // add instrumentAudios as plural data in payload
      for (InstrumentAudio instrumentAudio : instrumentAudios)
        payload.addData(payloadFactory.toPayloadObject(instrumentAudio));

      // if included, seek and add events to payload
      if (Objects.nonNull(include) && include.contains("events"))
        for (InstrumentAudioEvent instrumentAudioEvent : instrumentAudioEventDAO.readMany(hubAccess, instrumentAudioIds))
          payload.getIncluded().add(payloadFactory.toPayloadObject(instrumentAudioEvent));

      // if included, seek and add chords to payload
      if (Objects.nonNull(include) && include.contains("chords"))
        for (InstrumentAudioChord instrumentAudioChord : instrumentAudioChordDAO.readMany(hubAccess, instrumentAudioIds))
          payload.getIncluded().add(payloadFactory.toPayloadObject(instrumentAudioChord));

      // ok
      return response.ok(payload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one instrumentAudio

   @param payload with which to update InstrumentAudio record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(Payload payload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, payload);
  }

  /**
   Delete one InstrumentAudio by instrumentAudioId and bindingId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed({UserRoleType.ARTIST})
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private InstrumentAudioDAO dao() {
    return dao;
  }

}
