// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.segment;

import com.google.common.collect.ImmutableList;
import io.xj.core.access.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.model.Segment;
import io.xj.core.payload.Payload;
import io.xj.hub.HubResource;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

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


      // will only have value if this can parse a uuid from string
      // otherwise, ignore the exception on attempt and store a null value for uuid
      UUID uuidId;
      try {
        uuidId = UUID.fromString(chainId);
      } catch (Exception ignored) {
        uuidId = null;
      }

      // chain is either by uuid or embed key
      Collection<Segment> segments;
      if (Objects.nonNull(uuidId))
        segments = readAllSegmentsByChainId(Access.fromContext(crc)); // uuid
      else
        segments = readAllSegmentsByChainEmbedKey(); // embed key

      return response.ok(new Payload().setDataEntities(segments));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Read all segments by Chain Id, optionally of offset or seconds UTC

   @param access control
   @return segments
   @throws Exception on failure
   */
  private Collection<Segment> readAllSegmentsByChainId(Access access) throws Exception {

    if (Objects.nonNull(fromOffset))
      return dao().readAllFromOffset(access, UUID.fromString(chainId), fromOffset);

    if (Objects.nonNull(fromSecondsUTC))
      return dao().readAllFromSecondsUTC(access, UUID.fromString(chainId), fromSecondsUTC);

    return dao().readMany(access, ImmutableList.of(UUID.fromString(chainId)));
  }

  /**
   Read all segments by Chain Embed Key, optionally of offset or seconds UTC

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
   Get DAO of injector

   @return DAO
   */
  private SegmentDAO dao() {
    return injector.getInstance(SegmentDAO.class);
  }
}
