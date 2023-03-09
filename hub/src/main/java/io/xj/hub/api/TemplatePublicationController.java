// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubJsonapiEndpoint;
import io.xj.hub.access.HubAccess;
import io.xj.hub.ingest.HubIngestFactory;
import io.xj.hub.manager.TemplateManager;
import io.xj.hub.manager.TemplatePublicationManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePublication;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.PayloadDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.UUID;

/**
 * TemplatePublications
 */
@Path("api/1")
public class TemplatePublicationController extends HubJsonapiEndpoint {
  private final Logger LOG = LoggerFactory.getLogger(TemplatePublicationController.class);
  private final FileStoreProvider fileStoreProvider;
  private final TemplateManager templateManager;
  private final TemplatePublicationManager manager;
  private final HubIngestFactory ingestFactory;
  private final int templatePublicationCacheExpireSeconds;
  private final String audioBucket;
  private final JsonProvider jsonProvider;

  /**
   * Constructor
   */
  public TemplatePublicationController(
    EntityFactory entityFactory,
    AppEnvironment env,
    FileStoreProvider fileStoreProvider,
    HubSqlStoreProvider sqlStoreProvider,
    HubIngestFactory ingestFactory,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    TemplateManager templateManager,
    TemplatePublicationManager manager,
    JsonProvider jsonProvider) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);

    templatePublicationCacheExpireSeconds = env.getTemplatePublicationCacheExpireSeconds();
    audioBucket = env.getAudioFileBucket();

    this.manager = manager;
    this.fileStoreProvider = fileStoreProvider;
    this.ingestFactory = ingestFactory;
    this.templateManager = templateManager;
    this.jsonProvider = jsonProvider;
  }

  /**
   * Get all templatePublications.
   *
   * @param templateId to get templatePublications for
   * @return set of all templatePublications
   */
  @GET
  @Path("templates/{templateId}/publication")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readManyForTemplate(
    HttpServletRequest req, HttpServletResponse res,
    @PathParam("templateId") String templateId
  ) {
    try {
      HubAccess access = HubAccess.fromRequest(req);
      JsonapiPayload jsonapiPayload = new JsonapiPayload().setDataType(PayloadDataType.Many);
      Collection<TemplatePublication> templatePublications;

      // how we source templatePublications depends on the query parameters
      templatePublications = manager().readMany(access, ImmutableList.of(UUID.fromString(templateId)));

      // add templatePublications as plural data in payload
      for (TemplatePublication templatePublication : templatePublications)
        jsonapiPayload.addData(payloadFactory.toPayloadObject(templatePublication));

      return responseProvider.ok(jsonapiPayload);

    } catch (Exception e) {
      return responseProvider.failure(e);
    }
  }

  /**
   * Create new templatePublication
   *
   * @param jsonapiPayload with which to update TemplatePublication record.
   * @return ResponseEntity
   */
  @POST
  @Path("template-publications")
  @Consumes(MediaType.APPLICATION_JSON_VALUE)
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(JsonapiPayload jsonapiPayload, HttpServletRequest req, HttpServletResponse res) {
    try {
      var access = HubAccess.fromRequest(req);
      TemplatePublication templatePublication = payloadFactory.consume(manager().newInstance(), jsonapiPayload);
      Template template = templateManager.readOne(access, templatePublication.getTemplateId());
      String publicationKey = String.format("%s.%s", template.getShipKey(), FileStoreProvider.EXTENSION_JSON);
      var payload = ingestFactory.ingest(access, templatePublication.getTemplateId()).toContentPayload();
      String content = jsonProvider.getMapper().writeValueAsString(payload);

      LOG.debug("Will upload {} bytes to s3://{}/{}", content.length(), audioBucket, publicationKey);
      fileStoreProvider.putS3ObjectFromString(content, audioBucket, publicationKey, MediaType.APPLICATION_JSON_VALUE, templatePublicationCacheExpireSeconds);
      LOG.info("Did upload {} bytes to s3://{}/{}", content.length(), audioBucket, publicationKey);

      TemplatePublication created = manager().create(
        HubAccess.fromRequest(req),
        templatePublication);

      return responseProvider.create(new JsonapiPayload().setDataOne(payloadFactory.toPayloadObject(created)));

    } catch (Exception e) {
      LOG.error("Failed to publish!", e);
      return responseProvider.notAcceptable(e);
    }
  }

  /**
   * Get Manager of injector
   *
   * @return Manager
   */
  private TemplatePublicationManager manager() {
    return manager;
  }
}
