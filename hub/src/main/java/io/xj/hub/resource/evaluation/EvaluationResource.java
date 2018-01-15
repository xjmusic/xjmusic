// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.evaluation;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.evaluation.Digest;
import io.xj.core.evaluation.DigestFactory;
import io.xj.core.evaluation.DigestType;
import io.xj.core.evaluation.EvaluationFactory;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.library.Library;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;

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
 [#154234716] Architect wants evaluation of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
@Path("evaluation")
public class EvaluationResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final EvaluationFactory evaluation = injector.getInstance(EvaluationFactory.class);
  private final DigestFactory digest = injector.getInstance(DigestFactory.class);

  @QueryParam("libraryId")
  String libraryIdString;

  @QueryParam("type")
  String typeString;

  /**
   Perform any type of evaluation

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({UserRoleType.ARTIST, UserRoleType.ENGINEER})
  public Response doEvaluation(@Context ContainerRequestContext crc) throws Exception {

    if (Objects.isNull(libraryIdString) || libraryIdString.isEmpty()) {
      return response.notAcceptable("Must specify `libraryId` of evaluation.");
    }

    if (Objects.isNull(typeString) || typeString.isEmpty()) {
      return response.notAcceptable("Must specify `type` of evaluation.");
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
   Perform any type of evaluation

   @param access   control
   @param type     of evaluation
   @param targetId of entity
   @return evaluation
   */
  private Digest evaluate(Access access, DigestType type, BigInteger targetId) throws Exception {
    switch (type) {

      case DigestMemes:
        return digest.memesOf(evaluation.of(access, ImmutableList.of(new Library(targetId))));

      case DigestChords:
        return digest.chordsOf(evaluation.of(access, ImmutableList.of(new Library(targetId))));

      default:
        throw new BusinessException(String.format("Invalid type: %s", type));
    }
  }
}
