// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.Segment;
import io.xj.lib.entity.Entities;
import io.xj.lib.jsonapi.HttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.PayloadDataType;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.util.Value;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.NexusEndpoint;
import io.xj.service.nexus.dao.SegmentDAO;

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
    @QueryParam("chainId") String chainIdentifier,
    @QueryParam("fromOffset") Long fromOffset,
    @QueryParam("fromSecondsUTC") Long fromSecondsUTC,
    @QueryParam("detailed") Boolean detailed
  ) {
    if (Objects.isNull(chainIdentifier) || chainIdentifier.isEmpty())
      return response.notAcceptable("Chain id is required");

    try {
      // will only have value if this can parse a uuid from string
      // otherwise, ignore the exception on attempt and store a null value for uuid
      String uuidId;
      try {
        uuidId = UUID.fromString(chainIdentifier).toString();
      } catch (Exception ignored) {
        uuidId = null;
      }

      // chain is either by uuid or embed key
      Collection<Segment> segments;
      if (Value.isNonNull(uuidId))
        segments = readManySegmentsByChainId(HubClientAccess.fromContext(crc), uuidId, fromOffset, fromSecondsUTC); // uuid
      else
        segments = readManySegmentsByChainEmbedKey(chainIdentifier, fromOffset, fromSecondsUTC); // embed key

      // Prepare payload
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);

      // add segments as plural data in payload
      for (Segment segment : segments) jsonapiPayload.addData(payloadFactory.toPayloadObject(segment));

      // seek and add sub-entities to payload --
      // use internal access because we already cleared these segment ids from access control,
      // and there is no access object when reading chain by embed key
      if (Objects.nonNull(detailed) && detailed)
        for (Object entity : dao().readManySubEntities(HubClientAccess.internal(), Entities.idsOf(segments), false))
          jsonapiPayload.getIncluded().add(payloadFactory.toPayloadObject(entity));

      // done
      return response.ok(jsonapiPayload);

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
  @RolesAllowed(USER)
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
      return dao().readManyFromOffset(access, chainId, fromOffset);

    if (Objects.nonNull(fromSecondsUTC))
      return dao().readManyFromSecondsUTC(access, chainId, fromSecondsUTC);

    return dao().readMany(access, ImmutableList.of(chainId));
  }

  /**
   Read all segments by Chain Embed Key, optionally of offset or seconds UTC
   <p>
   FUTURE pass access down through here-- child processes need to know not to require access for embed-key based things
   See: [#173806398] Must be able to read chain elements publicly by embed key

   @param embedKey       to read segments for
   @param fromOffset     from which to read segments
   @param fromSecondsUTC from which to read segments
   @return segments
   @throws Exception on failure
   */
  private Collection<Segment> readManySegmentsByChainEmbedKey(String embedKey, Long fromOffset, Long fromSecondsUTC) throws Exception {

    if (Objects.nonNull(fromOffset))
      return dao().readManyFromOffsetByEmbedKey(HubClientAccess.internal(), embedKey, fromOffset);

    if (Objects.nonNull(fromSecondsUTC))
      return dao().readManyFromSecondsUTCbyEmbedKey(HubClientAccess.internal(), embedKey, fromSecondsUTC);

    return dao().readManyByEmbedKey(HubClientAccess.internal(), embedKey);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private SegmentDAO dao() {
    return dao;
  }
}
