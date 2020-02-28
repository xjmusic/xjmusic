// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.app.AppResource;
import io.xj.lib.core.dao.DAO;
import io.xj.lib.core.dao.SegmentDAO;
import io.xj.lib.core.model.Segment;
import io.xj.lib.core.model.UserRoleType;
import io.xj.lib.core.payload.Payload;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
public class SegmentEndpoint extends AppResource {
  private SegmentDAO dao;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public SegmentEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(SegmentDAO.class);
  }

  /**
   Get all segments.

   @return application/json response.
   */
  @GET
  @PermitAll
  public Response readAll(
    @Context ContainerRequestContext crc,
    @QueryParam("chainId") String chainId,
    @QueryParam("fromOffset") Long fromOffset,
    @QueryParam("fromSecondsUTC") Long fromSecondsUTC
  ) {
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

      // Prepare payload
      Payload payload = new Payload();

      // chain is either by uuid or embed key
      Collection<Segment> segments;
      if (Objects.nonNull(uuidId))
        segments = readAllSegmentsByChainId(Access.fromContext(crc), chainId, fromOffset, fromSecondsUTC); // uuid
      else
        segments = readAllSegmentsByChainEmbedKey(chainId, fromOffset, fromSecondsUTC); // embed key

      // add segments as plural data in payload
      payload.setDataEntities(segments);

      // seek and add sub-entities to payload --
      // use internal access because we already cleared these segment ids from access control,
      // and there is no access object when reading chain by embed key
      dao().readAllSubEntities(Access.internal(), DAO.idsFrom(segments), false)
        .forEach(entity -> payload.addIncluded(entity.toPayloadObject()));

      // done
      return response.ok(payload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Get one segment.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }


  /**
   Read all segments by Chain Id, optionally of offset or seconds UTC

   @param access         control
   @param chainId        to read segments for
   @param fromOffset     from which to read segments
   @param fromSecondsUTC from which to read segments
   @return segments
   @throws Exception on failure
   */
  private Collection<Segment> readAllSegmentsByChainId(Access access, String chainId, Long fromOffset, Long fromSecondsUTC) throws Exception {

    if (Objects.nonNull(fromOffset))
      return dao().readAllFromOffset(access, UUID.fromString(chainId), fromOffset);

    if (Objects.nonNull(fromSecondsUTC))
      return dao().readAllFromSecondsUTC(access, UUID.fromString(chainId), fromSecondsUTC);

    return dao().readMany(access, ImmutableList.of(UUID.fromString(chainId)));
  }

  /**
   Read all segments by Chain Embed Key, optionally of offset or seconds UTC

   @param chainId        to read segments for
   @param fromOffset     from which to read segments
   @param fromSecondsUTC from which to read segments
   @return segments
   @throws Exception on failure
   */
  private Collection<Segment> readAllSegmentsByChainEmbedKey(String chainId, Long fromOffset, Long fromSecondsUTC) throws Exception {

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
    return dao;
  }
}
