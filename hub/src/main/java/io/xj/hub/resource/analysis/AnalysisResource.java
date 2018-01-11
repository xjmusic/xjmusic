// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.analysis;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.analysis.AnalysisProvider;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.analysis.Analysis;
import io.xj.core.model.analysis.AnalysisType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.server.HttpResponseProvider;

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
 Analysis
 <p>
 [#154234716] Architect wants analysis of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 */
@Path("analysis")
public class AnalysisResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);
  private final AnalysisProvider analysis = injector.getInstance(AnalysisProvider.class);

  @QueryParam("entityId")
  String entityIdString;

  @QueryParam("type")
  String typeString;

  /**
   Perform any type of analysis

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({UserRoleType.ARTIST, UserRoleType.ENGINEER})
  public Response doAnalysis(@Context ContainerRequestContext crc) throws Exception {

    if (Objects.isNull(entityIdString) || entityIdString.isEmpty()) {
      return response.notAcceptable("Must specify `entityIdString` of analysis.");
    }

    if (Objects.isNull(typeString) || typeString.isEmpty()) {
      return response.notAcceptable("Must specify `typeString` of analysis.");
    }

    AnalysisType type;
    try {
      type = AnalysisType.validate(typeString);
    } catch (Exception e) {
      return response.failure(e);
    }

    BigInteger entityId = new BigInteger(entityIdString);
    if (!Objects.equals(1, entityId.compareTo(BigInteger.ZERO))) {
      return response.notAcceptable("`entityId` must be greater than zero");
    }

    try {
      return response.readOne(
        Analysis.KEY_ONE,
        analyze(
          Access.fromContext(crc),
          type, entityId
        ).toJSONObject());

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Perform any type of analysis

   @param access   control
   @param type     of analysis
   @param targetId of entity
   @return analysis
   */
  private Analysis analyze(Access access, AnalysisType type, BigInteger targetId) throws Exception {
    switch (type) {

      case LibraryMeme:
        return analysis.analyzeLibraryMemes(access, targetId);

      case LibraryChord:
        return analysis.analyzeLibraryChords(access, targetId);

      default:
        throw new BusinessException(String.format("Invalid type: %s", type));
    }
  }
}
