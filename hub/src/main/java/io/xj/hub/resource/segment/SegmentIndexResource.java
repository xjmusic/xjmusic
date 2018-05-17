// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.segment;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.SegmentChordDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.dao.SegmentMemeDAO;
import io.xj.core.dao.SegmentMessageDAO;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.chord.Chord;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.message.Message;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.core.transport.JSON;
import io.xj.core.util.Text;
import io.xj.hub.HubResource;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import org.json.JSONArray;

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
  private final ChoiceDAO choiceDAO = injector.getInstance(ChoiceDAO.class);
  private final ArrangementDAO arrangementDAO = injector.getInstance(ArrangementDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final SegmentChordDAO segmentChordDAO = injector.getInstance(SegmentChordDAO.class);
  private final SegmentDAO segmentDAO = injector.getInstance(SegmentDAO.class);
  private final SegmentMemeDAO segmentMemeDAO = injector.getInstance(SegmentMemeDAO.class);
  private final SegmentMessageDAO segmentMessageDAO = injector.getInstance(SegmentMessageDAO.class);

  @QueryParam("chainId")
  String chainId;

  @QueryParam("include")
  String include;

  @QueryParam("fromOffset")
  BigInteger fromOffset;

  @QueryParam("fromSecondsUTC")
  BigInteger fromSecondsUTC;

  /**
   Get an immutable list of ids from a result of Segments

   @param segments to get ids of
   @return list of ids
   */
  private static Collection<BigInteger> segmentIds(Iterable<Segment> segments) {
    ImmutableList.Builder<BigInteger> builder = ImmutableList.builder();
    segments.forEach(segment -> builder.add(segment.getId()));
    return builder.build();
  }

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
        .accepted(JSON.wrap(readAllIncludingRelationships(Access.fromContext(crc))).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Read all segments, including reading relations request in the `?include=` query parameter

   @param access control
   @return map of entity plural key to array of entities
   @throws Exception on failure
   */
  private Map<String, JSONArray> readAllIncludingRelationships(Access access) throws Exception {
    Map<String, JSONArray> out = Maps.newHashMap();

    Collection<Segment> segments;
    if (Text.isInteger(chainId)) segments = readAllSegmentsByChainId(access);
    else segments = readAllSegmentsByChainEmbedKey();
    out.put(Segment.KEY_MANY, JSON.arrayOf(segments));
    Collection<BigInteger> segmentIds = segmentIds(segments);

    if (Objects.nonNull(include) && include.contains(Message.KEY_MANY))
      out.put(SegmentMessage.KEY_MANY, JSON.arrayOf(segmentMessageDAO.readAll(access, segmentIds)));

    if (Objects.nonNull(include) && include.contains(Meme.KEY_MANY))
      out.put(SegmentMeme.KEY_MANY, JSON.arrayOf(segmentMemeDAO.readAllInSegments(access, segmentIds)));

    if (Objects.nonNull(include) && include.contains(Chord.KEY_MANY))
      out.put(SegmentChord.KEY_MANY, JSON.arrayOf(segmentChordDAO.readAllInSegments(access, segmentIds)));

    if (Objects.nonNull(include) && include.contains(Choice.KEY_MANY))
      out.put(Choice.KEY_MANY, JSON.arrayOf(choiceDAO.readAllInSegments(access, segmentIds)));

    if (Objects.nonNull(include) && include.contains(Arrangement.KEY_MANY))
      out.put(Arrangement.KEY_MANY, JSON.arrayOf(arrangementDAO.readAllInSegments(access, segmentIds)));

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
