// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.sequence_pattern;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequencePatternDAO;
import io.xj.core.dao.SequencePatternMemeDAO;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.sequence_pattern.SequencePattern;
import io.xj.core.model.sequence_pattern.SequencePatternWrapper;
import io.xj.core.model.sequence_pattern_meme.SequencePatternMeme;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.core.transport.JSON;
import io.xj.hub.HubResource;
import org.json.JSONArray;

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
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 Sequence record
 */
@Path("sequence-patterns")
public class SequencePatternIndexResource extends HubResource {
  private final SequencePatternDAO sequencePatternDAO = injector.getInstance(SequencePatternDAO.class);
  private final SequencePatternMemeDAO sequencePatternMemeDAO = injector.getInstance(SequencePatternMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("sequenceId")
  String sequenceId;

  @QueryParam("include")
  String include;

  /**
   Get an immutable list of ids from a result of SequencePatterns

   @param sequencePatterns to get ids of
   @return list of ids
   */
  private static Collection<BigInteger> sequencePatternIds(Iterable<SequencePattern> sequencePatterns) {
    ImmutableList.Builder<BigInteger> builder = ImmutableList.builder();
    sequencePatterns.forEach(sequencePattern -> builder.add(sequencePattern.getId()));
    return builder.build();
  }

  /**
   Get all sequence patterns in a sequence, optionally including relationships

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (Objects.isNull(sequenceId) || sequenceId.isEmpty()) {
      return response.notAcceptable("Sequence id is required");
    }

    try {
      return Response
        .accepted(JSON.wrap(readAllIncludingRelationships(Access.fromContext(crc))).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Read all sequencePatterns, including reading relations request in the `?include=` query parameter

   @param access control
   @return map of entity plural key to array of chords
   @throws Exception on failure
   */
  private Map<String, JSONArray> readAllIncludingRelationships(Access access) throws Exception {
    Map<String, JSONArray> out = Maps.newHashMap();

    Collection<SequencePattern> sequencePatterns = sequencePatternDAO.readAll(access, ImmutableList.of(new BigInteger(sequenceId)));
    out.put(SequencePattern.KEY_MANY, JSON.arrayOf(sequencePatterns));
    Collection<BigInteger> sequencePatternIds = sequencePatternIds(sequencePatterns);

    if (Objects.nonNull(include) && include.contains(Meme.KEY_MANY))
      out.put(SequencePatternMeme.KEY_MANY, JSON.arrayOf(sequencePatternMemeDAO.readAll(access, sequencePatternIds)));

    return out;
  }


  /**
   Create new sequence pattern

   @param data with which to update Sequence record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(SequencePatternWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        SequencePattern.KEY_MANY,
        SequencePattern.KEY_ONE,
        sequencePatternDAO.create(
          Access.fromContext(crc),
          data.getSequencePattern()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
