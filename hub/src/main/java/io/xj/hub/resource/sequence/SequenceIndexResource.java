// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.sequence;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequenceDAO;
import io.xj.core.model.library.Library;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.craft.generation.GenerationFactory;
import io.xj.core.ingest.IngestFactory;
import io.xj.hub.HubResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Objects;

/**
 Sequences
 */
@Path("sequences")
public class SequenceIndexResource extends HubResource {
  private final Logger log = LoggerFactory.getLogger(SequenceIndexResource.class);
  private final SequenceDAO sequenceDAO = injector.getInstance(SequenceDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final GenerationFactory generationFactory = injector.getInstance(GenerationFactory.class);
  private final IngestFactory ingestFactory = injector.getInstance(IngestFactory.class);

  @QueryParam("accountId")
  String accountId;

  @QueryParam("libraryId")
  String libraryId;

  @QueryParam("cloneId")
  String cloneId;

  @QueryParam("generateLibrarySupersequence")
  Boolean generateLibrarySupersequence;

  /**
   Get all sequences.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.nonNull(libraryId) && !libraryId.isEmpty()) {
      return readAllInLibrary(Access.fromContext(crc));

    } else if (Objects.nonNull(accountId) && !accountId.isEmpty()) {
      return readAllInAccount(Access.fromContext(crc));

    } else {
      return readAll(Access.fromContext(crc));
    }
  }

  private Response readAllInAccount(Access access) {
    try {
      return response.readMany(
        Sequence.KEY_MANY,
        sequenceDAO.readAllInAccount(
          access,
          new BigInteger(accountId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAllInLibrary(Access access) {
    try {
      return response.readMany(
        Sequence.KEY_MANY,
        sequenceDAO.readAll(
          access,
          ImmutableList.of(new BigInteger(libraryId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  private Response readAll(Access access) {
    try {
      return response.readMany(
        Sequence.KEY_MANY,
        sequenceDAO.readAll(
          access));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new sequence.
   <p>
   [#154548999] if query parameter ?generateLibrarySupersequence=true then generate a Library Supersequence

   @param data with which to update Sequence record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(SequenceWrapper data, @Context ContainerRequestContext crc) {
    try {
      Sequence created;

      if (Objects.nonNull(cloneId)) created = sequenceDAO.clone(
        Access.fromContext(crc),
        new BigInteger(cloneId),
        data.getSequence());

      else created = sequenceDAO.create(
        Access.fromContext(crc),
        data.getSequence());

      if (Objects.nonNull(generateLibrarySupersequence) && generateLibrarySupersequence)
        log.info("Generated Library Super-sequence: {}",
          generationFactory.librarySupersequence(created,
            ingestFactory.evaluate(Access.fromContext(crc),
              ImmutableList.of(new Library(created.getLibraryId()))
            )).toJSONObject());

      return response.create(
        Sequence.KEY_MANY,
        Sequence.KEY_ONE,
        created);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
