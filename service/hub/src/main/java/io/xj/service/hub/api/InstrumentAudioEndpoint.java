// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.xj.lib.rest_api.MediaType;
import io.xj.lib.rest_api.Payload;
import io.xj.lib.rest_api.PayloadDataType;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.DAO;
import io.xj.service.hub.dao.InstrumentAudioChordDAO;
import io.xj.service.hub.dao.InstrumentAudioDAO;
import io.xj.service.hub.dao.InstrumentAudioEventDAO;
import io.xj.service.hub.model.InstrumentAudio;
import io.xj.service.hub.model.InstrumentAudioChord;
import io.xj.service.hub.model.InstrumentAudioEvent;
import io.xj.service.hub.model.UserRoleType;

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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

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
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed({UserRoleType.ARTIST})
  public Response create(Payload payload, @Context ContainerRequestContext crc, @QueryParam("cloneId") String cloneId) {
    try {
      Access access = Access.fromContext(crc);
      InstrumentAudio instrumentAudio = payloadFactory.consume(dao().newInstance(), payload);
      InstrumentAudio created;
      if (Objects.nonNull(cloneId))
        created = dao().clone(access, UUID.fromString(cloneId), instrumentAudio);
      else
        created = dao().create(access, instrumentAudio);

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
      Map<String, String> result = dao().authorizeUpload(Access.fromContext(crc), UUID.fromString(id));
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
  public Response readAll(@Context ContainerRequestContext crc, @QueryParam("instrumentId") String instrumentId, @QueryParam("include") String include) {
    try {
      Access access = Access.fromContext(crc);
      Payload payload = new Payload().setDataType(PayloadDataType.HasMany);
      Collection<InstrumentAudio> instrumentAudios = dao.readMany(Access.fromContext(crc), ImmutableList.of(UUID.fromString(instrumentId)));
      Set<UUID> instrumentAudioIds = DAO.idsFrom(instrumentAudios);

      // add instrumentAudios as plural data in payload
      for (InstrumentAudio instrumentAudio : instrumentAudios)
        payload.addData(payloadFactory.toPayloadObject(instrumentAudio));

      // if included, seek and add events to payload
      if (Objects.nonNull(include) && include.contains("events"))
        for (InstrumentAudioEvent instrumentAudioEvent : instrumentAudioEventDAO.readMany(access, instrumentAudioIds))
          payload.getIncluded().add(payloadFactory.toPayloadObject(instrumentAudioEvent));

      // if included, seek and add chords to payload
      if (Objects.nonNull(include) && include.contains("chords"))
        for (InstrumentAudioChord instrumentAudioChord : instrumentAudioChordDAO.readMany(access, instrumentAudioIds))
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
  @Consumes(MediaType.APPLICATION_JSON_API)
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
