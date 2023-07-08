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
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.PayloadDataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.UUID;

/**
 * TemplatePublications
 */
@RestController
@RequestMapping("/api/1")
public class TemplatePublicationController extends HubJsonapiEndpoint {
  final Logger LOG = LoggerFactory.getLogger(TemplatePublicationController.class);
  final FileStoreProvider fileStoreProvider;
  final TemplateManager templateManager;
  final TemplatePublicationManager manager;
  final HubIngestFactory ingestFactory;
  final int templatePublicationCacheExpireSeconds;
  final String audioBucket;
  final JsonProvider jsonProvider;

  /**
   * Constructor
   */
  @Autowired
  public TemplatePublicationController(
    EntityFactory entityFactory,
    FileStoreProvider fileStoreProvider,
    HubSqlStoreProvider sqlStoreProvider,
    HubIngestFactory ingestFactory,
    JsonapiResponseProvider response,
    JsonapiPayloadFactory payloadFactory,
    TemplateManager templateManager,
    TemplatePublicationManager manager,
    JsonProvider jsonProvider,
    @Value("${template.publication.cache.expire.seconds}")
    int templatePublicationCacheExpireSeconds,
    @Value("${audio.file.bucket}")
    String audioBucket
  ) {
    super(sqlStoreProvider, response, payloadFactory, entityFactory);
    this.manager = manager;
    this.fileStoreProvider = fileStoreProvider;
    this.ingestFactory = ingestFactory;
    this.templateManager = templateManager;
    this.jsonProvider = jsonProvider;
    this.templatePublicationCacheExpireSeconds = templatePublicationCacheExpireSeconds;
    this.audioBucket = audioBucket;
  }

  /**
   * Get all templatePublications.
   *
   * @param templateId to get templatePublications for
   * @return set of all templatePublications
   */
  @GetMapping("templates/{templateId}/publication")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> readManyForTemplate(
    HttpServletRequest req,
    @PathVariable("templateId") String templateId
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
  @PostMapping("template-publications")
  @RolesAllowed(ARTIST)
  public ResponseEntity<JsonapiPayload> create(@RequestBody JsonapiPayload jsonapiPayload, HttpServletRequest req) {
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
  TemplatePublicationManager manager() {
    return manager;
  }
}
