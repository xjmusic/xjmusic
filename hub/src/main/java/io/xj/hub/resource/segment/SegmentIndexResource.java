// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.segment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Exposure;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.core.util.Text;
import io.xj.hub.HubResource;

import javax.annotation.security.PermitAll;
import javax.jws.WebResult;
import javax.ws.rs.GET;
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
 Segments
 */
@Path("segments")
public class SegmentIndexResource extends HubResource {
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final SegmentDAO segmentDAO = injector.getInstance(SegmentDAO.class);

  @QueryParam("chainId")
  String chainId;

  @QueryParam("include")
  String include;

  @QueryParam("fromOffset")
  BigInteger fromOffset;

  @QueryParam("fromSecondsUTC")
  BigInteger fromSecondsUTC;

  /**
   Get all segments.

   @return application/json response.
   */
  @GET
  @WebResult
  @PermitAll
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(chainId) || chainId.isEmpty())
      return response.notAcceptable("Chain id is required");

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
   Read all segments

   @param access control
   @return map of entity plural key to array of entities
   @throws Exception on failure
   */
  private Map<String, Collection> readAllIncludingRelationships(Access access) throws Exception {
    Map<String, Collection> out = Maps.newHashMap();

    Collection<Segment> segments = Text.isInteger(chainId) ? readAllSegmentsByChainId(access) : readAllSegmentsByChainEmbedKey();

    out.put(Segment.KEY_MANY, segments);

    if (Objects.nonNull(include) && include.contains(Exposure.MESSAGES))
      out.put(Exposure.SEGMENT_MESSAGES, SegmentMessage.aggregate(segments));

    if (Objects.nonNull(include) && include.contains(Exposure.MEMES))
      out.put(Exposure.SEGMENT_MEMES, SegmentMeme.aggregate(segments));

    if (Objects.nonNull(include) && include.contains(Exposure.CHORDS))
      out.put(Exposure.SEGMENT_CHORDS, SegmentChord.aggregate(segments));

    if (Objects.nonNull(include) && include.contains(Exposure.CHOICES))
      out.put(Exposure.CHOICES, Choice.aggregate(segments));

    if (Objects.nonNull(include) && include.contains(Exposure.ARRANGEMENTS))
      out.put(Exposure.ARRANGEMENTS, Arrangement.aggregate(segments));

    if (Objects.nonNull(include) && include.contains(Exposure.PICKS))
      out.put(Exposure.PICKS, Pick.aggregate(segments));

    return out;
  }

  /**
   Read all segments by Chain Id, optionally from offset or seconds UTC

   @param access control
   @return segments
   @throws Exception on failure
   */
  private Collection<Segment> readAllSegmentsByChainId(Access access) throws Exception {

    if (Objects.nonNull(fromOffset))
      return segmentDAO.readAllFromOffset(access, new BigInteger(chainId), fromOffset);

    if (Objects.nonNull(fromSecondsUTC))
      return segmentDAO.readAllFromSecondsUTC(access, new BigInteger(chainId), fromSecondsUTC);

    return segmentDAO.readAll(access, ImmutableList.of(new BigInteger(chainId)));
  }

  /**
   Read all segments by Chain Embed Key, optionally from offset or seconds UTC

   @return segments
   @throws Exception on failure
   */
  private Collection<Segment> readAllSegmentsByChainEmbedKey() throws Exception {

    if (Objects.nonNull(fromOffset))
      return segmentDAO.readAllFromOffset(chainId, fromOffset);

    if (Objects.nonNull(fromSecondsUTC))
      return segmentDAO.readAllFromSecondsUTC(chainId, fromSecondsUTC);

    return segmentDAO.readAll(chainId);
  }

}
