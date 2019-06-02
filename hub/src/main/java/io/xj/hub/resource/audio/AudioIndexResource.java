// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.audio;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Exposure;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio.AudioWrapper;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.event.Event;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
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
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 Audios
 */
@Path("audios")
public class AudioIndexResource extends HubResource {
  private final AudioDAO audioDAO = injector.getInstance(AudioDAO.class);
  private final AudioEventDAO audioEventDAO = injector.getInstance(AudioEventDAO.class);
  private final AudioChordDAO audioChordDAO = injector.getInstance(AudioChordDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("instrumentId")
  String instrumentId;

  @QueryParam("cloneId")
  String cloneId;

  @QueryParam("include")
  String include;

  /**
   Get an immutable list of ids from a result of Audios

   @param audios to get ids of
   @return list of ids
   */
  private static Collection<BigInteger> audioIds(Iterable<Audio> audios) {
    ImmutableList.Builder<BigInteger> builder = ImmutableList.builder();
    audios.forEach(audio -> builder.add(audio.getId()));
    return builder.build();
  }

  /**
   Get all audios

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (Objects.isNull(instrumentId) || instrumentId.isEmpty()) {
      return response.notAcceptable("Instrument id is required");
    }

    try {
      return Response
        .accepted(gsonProvider.gson().toJson(readAllIncludingRelationships(Access.fromContext(crc))))
        .type(MediaType.APPLICATION_JSON)
        .build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Read all audios, including reading relations request in the `?include=` query parameter

   @param access control
   @return map of entity plural key to array of chords
   @throws Exception on failure
   */
  private Map<String, Collection> readAllIncludingRelationships(Access access) throws Exception {
    Map<String, Collection> out = Maps.newHashMap();

    Collection<Audio> audios = audioDAO.readAll(access, ImmutableList.of(new BigInteger(instrumentId)));
    out.put(Audio.KEY_MANY, audios);
    Collection<BigInteger> audioIds = audioIds(audios);

    if (Objects.nonNull(include) && include.contains(Event.KEY_MANY))
      out.put(AudioEvent.KEY_MANY, audioEventDAO.readAll(access, audioIds));

    if (Objects.nonNull(include) && include.contains(Exposure.CHORDS))
      out.put(AudioChord.KEY_MANY, audioChordDAO.readAll(access, audioIds));

    return out;
  }

  /**
   Create new audio

   @param data with which to update Audio record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(AudioWrapper data, @Context ContainerRequestContext crc) {
    try {
      Audio created;
      if (Objects.nonNull(cloneId)) {
        created = audioDAO.clone(
          Access.fromContext(crc),
          new BigInteger(cloneId),
          data.getAudio());
      } else {
        created = audioDAO.create(
          Access.fromContext(crc),
          data.getAudio());
      }
      return response.create(
        Audio.KEY_MANY,
        Audio.KEY_ONE,
        created);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }


}
