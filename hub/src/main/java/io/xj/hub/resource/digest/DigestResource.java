// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.digest;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.library.Library;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.craft.digest.cache.DigestCacheProvider;
import io.xj.craft.ingest.cache.IngestCacheProvider;
import io.xj.craft.digest.Digest;
import io.xj.craft.digest.DigestType;
import io.xj.craft.ingest.Ingest;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.math.BigInteger;
import java.util.Objects;

/**
 Digest
 <p>
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
@Path("digest")
public class DigestResource extends HubResource {
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final IngestCacheProvider ingestProvider = injector.getInstance(IngestCacheProvider.class);
  private final DigestCacheProvider digestProvider = injector.getInstance(DigestCacheProvider.class);

  @QueryParam("libraryId")
  String libraryIdString;

  @QueryParam("type")
  String typeString;

  /**
   Perform any type of digest

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({UserRoleType.ARTIST, UserRoleType.ENGINEER})
  public Response digest(@Context ContainerRequestContext crc) throws Exception {

    if (Objects.isNull(libraryIdString) || libraryIdString.isEmpty()) {
      return response.notAcceptable("Must specify `libraryId` of digest.");
    }

    if (Objects.isNull(typeString) || typeString.isEmpty()) {
      return response.notAcceptable("Must specify `type` of digest.");
    }

    DigestType digestType;
    try {
      digestType = DigestType.validate(typeString);
    } catch (Exception e) {
      return response.failure(e);
    }

    BigInteger libraryId = new BigInteger(libraryIdString);
    if (!Objects.equals(1, libraryId.compareTo(BigInteger.ZERO))) {
      return response.notAcceptable("`libraryId` must be greater than zero");
    }

    try {
      return response.readOne(
        Digest.KEY_ONE,
        evaluate(
          Access.fromContext(crc),
          digestType, libraryId
        ).toJSONObject());

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
  private Digest evaluate(Access access, DigestType type, BigInteger targetId) throws Exception {
    Ingest ingest = ingestProvider.evaluate(access, ImmutableList.of(new Library(targetId)));
    switch (type) {

      case DigestHash:
        return digestProvider.hash(ingest);

      case DigestMeme:
        return digestProvider.meme(ingest);

      case DigestChordProgression:
        return digestProvider.chordProgression(ingest);

      case DigestChordMarkov:
        return digestProvider.chordMarkov(ingest);

      case DigestPatternStyle:
        return digestProvider.patternStyle(ingest);

      default:
        throw new BusinessException(String.format("Invalid type: %s", type));
    }
  }
}
