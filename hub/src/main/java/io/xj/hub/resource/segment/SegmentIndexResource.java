// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.segment;

import com.google.common.collect.ImmutableList;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.segment.Segment;
import io.xj.core.util.Value;
import io.xj.hub.HubResource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Objects;

/**
 Segments
 */
@Path("segments")
public class SegmentIndexResource extends HubResource {

  @QueryParam("chainId")
  String chainId;

  @QueryParam("fromOffset")
  Long fromOffset;

  @QueryParam("fromSecondsUTC")
  Long fromSecondsUTC;

  /**
   Get all segments.

   @return application/json response.
   */
  @GET
  @PermitAll
  public Response readAll(@Context ContainerRequestContext crc) {
    if (Objects.isNull(chainId) || chainId.isEmpty())
      return response.notAcceptable("Chain id is required");

    try {
      Collection<Segment> segments = Value.isInteger(chainId) ?
        readAllSegmentsByChainId(Access.fromContext(crc)) :
        readAllSegmentsByChainEmbedKey();

      return response.ok(new Payload().setDataEntities(segments, true));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Read all segments by Chain Id, optionally from offset or seconds UTC

   @param access control
   @return segments
   @throws Exception on failure
   */
  private Collection<Segment> readAllSegmentsByChainId(Access access) throws Exception {

    if (Objects.nonNull(fromOffset))
      return dao().readAllFromOffset(access, new BigInteger(chainId), fromOffset);

    if (Objects.nonNull(fromSecondsUTC))
      return dao().readAllFromSecondsUTC(access, new BigInteger(chainId), fromSecondsUTC);

    return dao().readMany(access, ImmutableList.of(new BigInteger(chainId)));
  }

  /**
   Read all segments by Chain Embed Key, optionally from offset or seconds UTC

   @return segments
   @throws Exception on failure
   */
  private Collection<Segment> readAllSegmentsByChainEmbedKey() throws Exception {

    if (Objects.nonNull(fromOffset))
      return dao().readAllFromOffset(chainId, fromOffset);

    if (Objects.nonNull(fromSecondsUTC))
      return dao().readAllFromSecondsUTC(chainId, fromSecondsUTC);

    return dao().readAll(chainId);
  }

  /**
   Get DAO from injector

   @return DAO
   */
  private SegmentDAO dao() {
    return injector.getInstance(SegmentDAO.class);
  }
}
