// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.digest.Digest;
import io.xj.service.hub.digest.DigestCacheProvider;
import io.xj.service.hub.digest.DigestType;
import io.xj.service.hub.ingest.Ingest;
import io.xj.service.hub.ingest.IngestCacheProvider;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.model.Library;
import io.xj.service.hub.model.UserRoleType;
import io.xj.lib.rest_api.Payload;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.UUID;

/**
 Digest
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
@Path("digest")
public class DigestEndpoint extends HubEndpoint {
  private IngestCacheProvider ingestProvider;
  private DigestCacheProvider digestProvider;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public DigestEndpoint(
    Injector injector
  ) {
    super(injector);
    ingestProvider = injector.getInstance(IngestCacheProvider.class);
    digestProvider = injector.getInstance(DigestCacheProvider.class);
  }

  /**
   Perform any type of digest

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ARTIST, UserRoleType.ENGINEER})
  public Response digest(
    @Context ContainerRequestContext crc,
    @QueryParam("type") String typeString,
    @QueryParam("libraryId") String libraryIdString
  ) {

    if (Objects.isNull(libraryIdString) || libraryIdString.isEmpty()) {
      return response.notAcceptable("Must specify `libraryId` create digest.");
    }

    if (Objects.isNull(typeString) || typeString.isEmpty()) {
      return response.notAcceptable("Must specify `type` create digest.");
    }

    DigestType digestType;
    try {
      digestType = DigestType.validate(typeString);
    } catch (Exception e) {
      return response.failure(e);
    }

    try {
      UUID libraryId = UUID.fromString(libraryIdString);
      Payload payload = new Payload();
      payload.setDataOne(evaluate(
        Access.fromContext(crc),
        digestType, libraryId
      ).getPayloadObject());
      return response.ok(payload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Perform any type of digest

   @param access   control
   @param type     of ingest
   @param targetId of entity
   @return ingest
   */
  private Digest evaluate(Access access, DigestType type, UUID targetId) throws Exception {
    Ingest ingest = ingestProvider.ingest(access, ImmutableList.of(ChainBinding.create(Chain.create(), new Library().setId(targetId))));
    switch (type) {

      case DigestHash:
        return digestProvider.hash(ingest);

      case DigestMeme:
        return digestProvider.meme(ingest);

      case DigestChordProgression:
        return digestProvider.chordProgression(ingest);

      case DigestChordMarkov:
        return digestProvider.chordMarkov(ingest);

      case DigestSequenceStyle:
        return digestProvider.sequenceStyle(ingest);

      default:
        throw new HubException(String.format("Invalid type: %s", type));
    }
  }
}
