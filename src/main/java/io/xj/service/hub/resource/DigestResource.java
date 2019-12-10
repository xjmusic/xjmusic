// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.resource;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.xj.core.access.Access;
import io.xj.core.app.AppResource;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.ingest.cache.IngestCacheProvider;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.Library;
import io.xj.core.model.UserRoleType;
import io.xj.core.payload.Payload;
import io.xj.craft.digest.Digest;
import io.xj.craft.digest.DigestCacheProvider;
import io.xj.craft.digest.DigestType;

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
public class DigestResource extends AppResource {
  private IngestCacheProvider ingestProvider;
  private DigestCacheProvider digestProvider;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public DigestResource(
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
        throw new CoreException(String.format("Invalid type: %s", type));
    }
  }
}
