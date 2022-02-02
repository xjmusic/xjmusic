// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.manager.TemplateManager;
import io.xj.hub.manager.TemplatePublicationManager;
import io.xj.hub.ingest.HubIngestFactory;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePublication;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.jsonapi.JsonapiHttpResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.PayloadDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.UUID;

import static io.xj.lib.jsonapi.MediaType.APPLICATION_JSONAPI;

/**
 TemplatePublications
 */
@Path("api/1")
public class TemplatePublicationEndpoint extends HubJsonapiEndpoint<TemplatePublication> {
  private final Logger LOG = LoggerFactory.getLogger(TemplatePublicationEndpoint.class);
  private final FileStoreProvider fileStoreProvider;
  private final TemplateManager templateManager;
  private final TemplatePublicationManager manager;
  private final HubIngestFactory ingestFactory;
  private final int templatePublicationCacheExpireSeconds;
  private final String audioBucket;

  /**
   Constructor
   */
  @Inject
  public TemplatePublicationEndpoint(
    EntityFactory entityFactory,
    Environment env,
    FileStoreProvider fileStoreProvider,
    HubDatabaseProvider dbProvider,
    HubIngestFactory ingestFactory,
    JsonapiHttpResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    TemplateManager templateManager,
    TemplatePublicationManager manager
  ) {
    super(dbProvider, response, payloadFactory, entityFactory);

    templatePublicationCacheExpireSeconds = env.getTemplatePublicationCacheExpireSeconds();
    audioBucket = env.getAudioFileBucket();

    this.manager = manager;
    this.fileStoreProvider = fileStoreProvider;
    this.ingestFactory = ingestFactory;
    this.templateManager = templateManager;
  }

  /**
   Get all templatePublications.

   @param templateId to get templatePublications for
   @return set of all templatePublications
   */
  @GET
  @Path("templates/{templateId}/publication")
  @RolesAllowed(ARTIST)
  public Response readManyForTemplate(
    @Context ContainerRequestContext crc,
    @PathParam("templateId") String templateId
  ) {
    try {
      HubAccess hubAccess = HubAccess.fromContext(crc);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<TemplatePublication> templatePublications;

      // how we source templatePublications depends on the query parameters
      templatePublications = manager().readMany(hubAccess, ImmutableList.of(UUID.fromString(templateId)));

      // add templatePublications as plural data in payload
      for (TemplatePublication templatePublication : templatePublications)
        jsonapiPayload.addData(payloadFactory.toPayloadObject(templatePublication));

      return response.ok(jsonapiPayload);

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new templatePublication

   @param jsonapiPayload with which to update TemplatePublication record.
   @return Response
   */
  @POST
  @Path("template-publications")
  @Consumes(APPLICATION_JSONAPI)
  @RolesAllowed(ARTIST)
  public Response create(JsonapiPayload jsonapiPayload, @Context ContainerRequestContext crc) {
    try {
      var access = HubAccess.fromContext(crc);
      TemplatePublication templatePublication = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      Template template = templateManager.readOne(access, templatePublication.getTemplateId());
      String publicationKey = String.format("%s.%s", template.getShipKey(), FileStoreProvider.EXTENSION_JSON);
      String content = ingestFactory.ingest(access, templatePublication.getTemplateId()).toJSON();

      LOG.debug("Will upload {} bytes to s3://{}/{}", content.length(), audioBucket, publicationKey);
      fileStoreProvider.putS3ObjectFromString(content, audioBucket, publicationKey, APPLICATION_JSONAPI, templatePublicationCacheExpireSeconds);
      LOG.info("Did upload {} bytes to s3://{}/{}", content.length(), audioBucket, publicationKey);

      TemplatePublication created = manager().create(
        HubAccess.fromContext(crc),
        templatePublication);

      return response.create(new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(created)));

    } catch (Exception e) {
      LOG.error("Failed to publish!", e);
      return response.notAcceptable(e);
    }
  }

  /**
   Get Manager of injector

   @return Manager
   */
  private TemplatePublicationManager manager() {
    return manager;
  }
}
