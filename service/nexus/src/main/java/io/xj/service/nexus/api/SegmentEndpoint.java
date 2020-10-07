// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.entity.Entity;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.entity.UserRoleType;
import io.xj.service.nexus.NexusEndpoint;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.entity.Segment;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
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
public class SegmentEndpoint extends NexusEndpoint {
  private final SegmentDAO dao;

  /**
   Constructor
   */
  @Inject
  public SegmentEndpoint(
    SegmentDAO dao,
    HttpResponseProvider response,
    Config config,
    PayloadFactory payloadFactory
  ) {
    super(response, config, payloadFactory);
    this.dao = dao;
  }

  /**
   Get all segments.

   @return application/json response.
   */
  @GET
  @PermitAll
  public Response readMany(
    @Context ContainerRequestContext crc,
    @QueryParam("chainId") String chainId,
    @QueryParam("fromOffset") Long fromOffset,
    @QueryParam("fromSecondsUTC") Long fromSecondsUTC,
    @QueryParam("detailed") Boolean detailed
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
      Payload payload = new Payload().setDataType(PayloadDataType.Many);

      // chain is either by uuid or embed key
      Collection<Segment> segments;
      if (Objects.nonNull(uuidId))
        segments = readManySegmentsByChainId(HubClientAccess.fromContext(crc), chainId, fromOffset, fromSecondsUTC); // uuid
      else
        segments = readManySegmentsByChainEmbedKey(chainId, fromOffset, fromSecondsUTC); // embed key

      // add segments as plural data in payload
      for (Segment segment : segments) payload.addData(payloadFactory.toPayloadObject(segment));

      // seek and add sub-entities to payload --
      // use internal access because we already cleared these segment ids from access control,
      // and there is no access object when reading chain by embed key
      if (Objects.nonNull(detailed) && detailed)
        for (Entity entity : dao().readManySubEntities(HubClientAccess.internal(), Entity.idsOf(segments), false))
          payload.getIncluded().add(payloadFactory.toPayloadObject(entity));

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
  private Collection<Segment> readManySegmentsByChainId(HubClientAccess access, String chainId, Long fromOffset, Long fromSecondsUTC) throws Exception {

    if (Objects.nonNull(fromOffset))
      return dao().readManyFromOffset(access, UUID.fromString(chainId), fromOffset);

    if (Objects.nonNull(fromSecondsUTC))
      return dao().readManyFromSecondsUTC(access, UUID.fromString(chainId), fromSecondsUTC);

    return dao().readMany(access, ImmutableList.of(UUID.fromString(chainId)));
  }

  /**
   Read all segments by Chain Embed Key, optionally of offset or seconds UTC
   <p>
   TODO pass access down through here-- child processes need to know not to require access for embed-key based things
   See: [#173806398] Must be able to read chain elements publicly by embed key

   @param embedKey       to read segments for
   @param fromOffset     from which to read segments
   @param fromSecondsUTC from which to read segments
   @return segments
   @throws Exception on failure
   */
  private Collection<Segment> readManySegmentsByChainEmbedKey(String embedKey, Long fromOffset, Long fromSecondsUTC) throws Exception {

    if (Objects.nonNull(fromOffset))
      return dao().readManyFromOffset(HubClientAccess.internal(), embedKey, fromOffset);

    if (Objects.nonNull(fromSecondsUTC))
      return dao().readManyFromSecondsUTC(HubClientAccess.internal(), embedKey, fromSecondsUTC);

    return dao().readMany(HubClientAccess.internal(), embedKey);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private SegmentDAO dao() {
    return dao;
  }
}
