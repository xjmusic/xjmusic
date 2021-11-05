// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessException;
import io.xj.hub.dao.DAOException;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.Feedback;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.*;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;

import javax.annotation.Nullable;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.UUID;

import static io.xj.hub.Tables.*;

/**
 Feedbacks
 */
@Path("api/1")
public class FeedbackEndpoint extends HubJsonapiEndpoint<Feedback> {

  /**
   Constructor
   */
  @Inject
  public FeedbackEndpoint(
    HubDatabaseProvider dbProvider,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    EntityFactory entityFactory
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);
  }

  /**
   Search for feedbacks

   @return application/json response.
   */
  @GET
  @Path("feedbacks")
  @RolesAllowed(USER)
  public Response readMany(
    @Context ContainerRequestContext crc,
    @QueryParam("accountId") UUID accountId,
    @Nullable @QueryParam("instrumentId") UUID instrumentId,
    @Nullable @QueryParam("libraryId") UUID libraryId,
    @Nullable @QueryParam("programId") UUID programId,
    @Nullable @QueryParam("templateId") UUID templateId,
    @Nullable @QueryParam("userId") UUID userId
  ) {
    try {
      HubAccess access = HubAccess.fromContext(crc);
      authorize(access, accountId);

      var from = dbProvider.getDSL()
        .select(FEEDBACK.fields())
        .from(FEEDBACK);

      if (Objects.nonNull(instrumentId))
        from = from.join(FEEDBACK_INSTRUMENT)
          .on(FEEDBACK_INSTRUMENT.FEEDBACK_ID.eq(FEEDBACK.ID))
          .and(FEEDBACK_INSTRUMENT.INSTRUMENT_ID.eq(instrumentId));

      if (Objects.nonNull(libraryId))
        from = from.join(FEEDBACK_LIBRARY)
          .on(FEEDBACK_LIBRARY.FEEDBACK_ID.eq(FEEDBACK.ID))
          .and(FEEDBACK_LIBRARY.LIBRARY_ID.eq(libraryId));

      if (Objects.nonNull(programId))
        from = from.join(FEEDBACK_PROGRAM)
          .on(FEEDBACK_PROGRAM.FEEDBACK_ID.eq(FEEDBACK.ID))
          .and(FEEDBACK_PROGRAM.PROGRAM_ID.eq(programId));

      if (Objects.nonNull(templateId))
        from = from.join(FEEDBACK_TEMPLATE)
          .on(FEEDBACK_TEMPLATE.FEEDBACK_ID.eq(FEEDBACK.ID))
          .and(FEEDBACK_TEMPLATE.TEMPLATE_ID.eq(templateId));

      var query = from.where(FEEDBACK.ACCOUNT_ID.eq(accountId));

      if (Objects.nonNull(userId))
        query = query.and(FEEDBACK.USER_ID.eq(userId));

      return response.create(new JsonapiPayload()
        .setDataMany(payloadFactory.toPayloadObjects(modelsFrom(Feedback.class, query.fetch()))));

    } catch (HubAccessException e) {
      return response.unauthorized();

    } catch (JsonapiException e) {
      return response.notAcceptable(e);

    } catch (DAOException e) {
      return response.failure(e);
    }
  }

  /**
   Create new feedback

   @param jsonapiPayload with which to update Feedback record.
   @return Response
   */
  @POST
  @Path("feedbacks")
  @Consumes(MediaType.APPLICATION_JSONAPI)
  @RolesAllowed({ADMIN, ENGINEER})
  public Response create(@Context ContainerRequestContext crc, JsonapiPayload jsonapiPayload) {
    try {
      HubAccess access = HubAccess.fromContext(crc);
      var entity = payloadFactory.consume(new Feedback(), jsonapiPayload);

      validate(entity);
      authorize(access, entity.getAccountId());

      return response.create(new JsonapiPayload()
        .setDataOne(payloadFactory.toPayloadObject(modelFrom(executeCreate(dbProvider.getDSL(), FEEDBACK, entity)))));

    } catch (HubAccessException e) {
      return response.unauthorized();

    } catch (JsonapiException | ValueException e) {
      return response.notAcceptable(e);

    } catch (DAOException e) {
      return response.failure(e);
    }
  }

  /**
   Authorize access for the given account id

   @param access    to authorize
   @param accountId attempt to access
   @throws HubAccessException if unauthorized
   */
  private void authorize(HubAccess access, @Nullable UUID accountId) throws HubAccessException {
    if (access.isTopLevel()) return;
    if (Objects.isNull(accountId))
      throw new HubAccessException("Cannot authorize without account id!");
    if (!access.getAccountIds().contains(accountId))
      throw new HubAccessException("Not authorized to create feedback for that account!");
  }

  /**
   Validate a new feedback

   @param feedback to validate
   @throws ValueException if invalid
   */
  private void validate(Feedback feedback) throws ValueException {
    Values.require(feedback.getAccountId(), "Account ID");
  }

}
