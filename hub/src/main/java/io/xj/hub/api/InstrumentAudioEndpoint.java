// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.InstrumentAudioManager;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.*;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 InstrumentAudio endpoint
 */
@Path("api/1/instrument-audios")
public class InstrumentAudioEndpoint extends HubJsonapiEndpoint<InstrumentAudio> {
  private final InstrumentAudioManager manager;

  /**
   Constructor
   */
  @Inject
  public InstrumentAudioEndpoint(
    InstrumentAudioManager manager,
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
  }

  /**
   Create new instrumentAudio binding

   @param jsonapiPayload with which to of InstrumentAudio Binding
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(
    JsonapiPayload jsonapiPayload,
    @Context ContainerRequestContext crc,
    @Nullable @QueryParam("cloneId") UUID cloneId
  ) {
    try {
      HubAccess access = HubAccess.fromContext(crc);
      var instrumentAudio = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      InstrumentAudio created;
      if (Objects.nonNull(cloneId))
        created = manager().clone(access, cloneId, instrumentAudio);
      else
        created = manager().create(access, instrumentAudio);

      return response.create(new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(created)));

    } catch (Exception e) {
      return response.notAcceptable(e);
    }
  }

  /**
   Generate an Upload policy to upload the corresponding file to 3rd-party storage (e.g. Amazon S3)

   @return application/json response.
   */
  @GET
  @Path("{id}/upload")
  @RolesAllowed(ARTIST)
  public Response uploadOne(@Context ContainerRequestContext crc, @PathParam("id") UUID id, @QueryParam("extension") String extension) {
    try {
      Map<String, String> result = manager().authorizeUpload(HubAccess.fromContext(crc), id, extension);
      if (null != result) {
        return Response
          .accepted(payloadFactory.serialize(result))
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return response.notFound(manager.newInstance().getClass(), id);
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
  @RolesAllowed(ARTIST)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") UUID id) {
    return readOne(crc, manager(), id);
  }

  /**
   Get Bindings in one instrumentAudio.

   @param detailed whether to include events and chords
   @return application/json response.
   */
  @GET
  @RolesAllowed(ARTIST)
  public Response readMany(
    @Context ContainerRequestContext crc,
    @QueryParam("instrumentId") String instrumentId,
    @QueryParam("detailed") Boolean detailed
  ) {
    try {
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<InstrumentAudio> instrumentAudios = manager.readMany(HubAccess.fromContext(crc), ImmutableList.of(UUID.fromString(instrumentId)));

      // add instrumentAudios as plural data in payload
      for (InstrumentAudio instrumentAudio : instrumentAudios)
        jsonapiPayload.addData(payloadFactory.toPayloadObject(instrumentAudio));

      // ok
      return response.ok(jsonapiPayload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one instrumentAudio

   @param jsonapiPayload with which to update InstrumentAudio record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response update(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc, @PathParam("id") UUID id) {
    return update(crc, manager(), id, jsonapiPayload);
  }

  /**
   Delete one InstrumentAudio by instrumentAudioId and bindingId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed(ARTIST)
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") UUID id) {
    return delete(crc, manager(), id);
  }

  /**
   Get Manager of injector

   @return Manager
   */
  private InstrumentAudioManager manager() {
    return manager;
  }

}
