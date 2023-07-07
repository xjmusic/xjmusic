// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.service;

import com.google.api.client.util.Lists;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.Logging.EntryListOption;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.run.v2.Container;
import com.google.cloud.run.v2.CreateServiceRequest;
import com.google.cloud.run.v2.DeleteServiceRequest;
import com.google.cloud.run.v2.EnvVar;
import com.google.cloud.run.v2.GetServiceRequest;
import com.google.cloud.run.v2.HTTPGetAction;
import com.google.cloud.run.v2.Probe;
import com.google.cloud.run.v2.ResourceRequirements;
import com.google.cloud.run.v2.RevisionScaling;
import com.google.cloud.run.v2.RevisionTemplate;
import com.google.cloud.run.v2.ServiceName;
import com.google.cloud.run.v2.ServicesClient;
import com.google.cloud.run.v2.UpdateServiceRequest;
import io.xj.hub.TemplateConfig;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

/**
 * Preview template functionality is tight (not wack)
 * <p>
 * Lab/Hub connects to service administration to manage a personal workload for preview templates
 * https://www.pivotaltracker.com/story/show/183576743
 * <p>
 * https://cloud.google.com/java/docs/reference/google-cloud-run/latest/overview
 */
@Service
public class PreviewNexusAdminImpl implements PreviewNexusAdmin {
  private static final String PREVIEW_NEXUS_DEPLOYMENT_FORMAT = "nexus-preview-%s-%s";
  private static final String RESOURCE_REQUIREMENT_KEY_CPU = "cpu";
  private static final String RESOURCE_REQUIREMENT_DEFAULT_CPU = "2";
  private static final String RESOURCE_REQUIREMENT_KEY_MEMORY = "memory";
  private static final String RESOURCE_REQUIREMENT_DEFAULT_MEMORY = "4Gi";
  private static final String LOG_LINE_FILTER_CONTAINS = "main]";
  private static final int LOG_LINE_TRIP_LEADING_CHARACTERS = 97;
  private final Logger LOG = LoggerFactory.getLogger(PreviewNexusAdminImpl.class);
  private final boolean isConfigured;
  private final String gcpServiceAccountEmail;
  private final String nexusImage;
  private final int logTailLines;
  private final String gcpProjectId;
  private final String gcpRegion;
  private final String appBaseUrl;
  private final String audioBaseUrl;
  private final String audioFileBucket;
  private final String audioUploadUrl;
  private final String awsAccessKeyId;
  private final String awsDefaultRegion;
  private final String awsSecretKey;
  private final String environment;
  private final String gcpCloudSqlInstance;
  private final String googleClientId;
  private final String googleClientSecret;
  private final String ingestTokenValue;
  private final String playerBaseUrl;
  private final String shipBaseUrl;
  private final String shipBucket;
  private final int logBackMinutes;

  @Autowired
  public PreviewNexusAdminImpl(
    @Value("${gcp.service.account.email}")
    String gcpServiceAccountEmail,
    @Value("${gcp.service.nexus.image}")
    String nexusImage,
    @Value("${service.log.tail.lines}")
    int logTailLines,
    @Value("${gcp.project.id}")
    String gcpProjectId,
    @Value("${gcp.region}")
    String gcpRegion,
    @Value("${app.base.url}")
    String appBaseUrl,
    @Value("${audio.base.url}")
    String audioBaseUrl,
    @Value("${audio.file.bucket}")
    String audioFileBucket,
    @Value("${audio.upload.url}")
    String audioUploadUrl,
    @Value("${aws.access.key.id}")
    String awsAccessKeyId,
    @Value("${aws.default.region}")
    String awsDefaultRegion,
    @Value("${aws.secret.key}")
    String awsSecretKey,
    @Value("${environment}")
    String environment,
    @Value("${gcp.cloud.sql.instance}")
    String gcpCloudSqlInstance,
    @Value("${google.client.id}")
    String googleClientId,
    @Value("${google.client.secret}")
    String googleClientSecret,
    @Value("${ingest.token.value}")
    String ingestTokenValue,
    @Value("${player.base.url}")
    String playerBaseUrl,
    @Value("${ship.base.url}")
    String shipBaseUrl,
    @Value("${ship.bucket}")
    String shipBucket,
    @Value("${service.logback.minutes}")
    int logBackMinutes,
    @Value("${service.container.env.secret.ref.name}")
    String envSecretRefName,
    @Value("${service.namespace}")
    String namespace
  ) {
    this.gcpServiceAccountEmail = gcpServiceAccountEmail;
    this.nexusImage = nexusImage;
    this.logTailLines = logTailLines;
    this.gcpProjectId = gcpProjectId;
    this.gcpRegion = gcpRegion;
    this.appBaseUrl = appBaseUrl;
    this.audioBaseUrl = audioBaseUrl;
    this.audioFileBucket = audioFileBucket;
    this.audioUploadUrl = audioUploadUrl;
    this.awsAccessKeyId = awsAccessKeyId;
    this.awsDefaultRegion = awsDefaultRegion;
    this.awsSecretKey = awsSecretKey;
    this.environment = environment;
    this.gcpCloudSqlInstance = gcpCloudSqlInstance;
    this.googleClientId = googleClientId;
    this.googleClientSecret = googleClientSecret;
    this.ingestTokenValue = ingestTokenValue;
    this.playerBaseUrl = playerBaseUrl;
    this.shipBaseUrl = shipBaseUrl;
    this.shipBucket = shipBucket;
    this.logBackMinutes = logBackMinutes;
    isConfigured = doConfigurationTest();
    LOG.info("Service administrator will create containers namespace={} with secretRef={}", namespace, envSecretRefName);
  }

  @Override
  public String getPreviewNexusLogs(TemplatePlayback playback) {
    if (!isConfigured)
      return "Service administrator is not configured!";

    try {
      var serviceName = computeServiceName(playback);

      List<String> lines = Lists.newArrayList();

      Instant now = Instant.now();
      Instant before = now.minus(Duration.ofMinutes(logBackMinutes));

      String filter = String.format("resource.type=\"cloud_run_revision\""
          + " AND resource.labels.service_name=\"%s\""
          + " AND timestamp >= \"%s\""
          + " AND timestamp <= \"%s\"",
        serviceName, before.toString(), now);

      Logging logging = LoggingOptions.getDefaultInstance().getService();
      EntryListOption entryListOption = EntryListOption.filter(filter);
      var entries = logging.listLogEntriesAsync(entryListOption).get();

      for (LogEntry logEntry : entries.iterateAll()) {
        if (logEntry.getPayload().getData().toString().contains(LOG_LINE_FILTER_CONTAINS)) {
          lines.add(logEntry.getPayload().getData().toString().substring(LOG_LINE_TRIP_LEADING_CHARACTERS));
        }
      }

      return String.join("\n", Values.last(logTailLines, lines).stream().toList());
    } catch (RuntimeException | InterruptedException | ExecutionException e) {
      LOG.error("Failed to get logs for preview nexus", e);
      return String.format("Failed to get logs for preview nexus: %s", e.getMessage());
    }
  }

  @Override
  public void startPreviewNexus(Template template, TemplatePlayback playback) throws ServiceException {
    if (!isConfigured) {
      LOG.warn("Service administrator is not configured!");
      return;
    }
    if (!previewNexusExists(playback)) {
      createPreviewNexus(template, playback);
    } else {
      updatePreviewNexus(template, playback);
    }
  }

  @Override
  public void stopPreviewNexus(TemplatePlayback playback) {
    if (!isConfigured) {
      LOG.warn("Service administrator is not configured!");
      return;
    }
    if (!previewNexusExists(playback)) return;
    deletePreviewNexus(playback);
  }

  @Override
  public boolean isReady() {
    return isConfigured;
  }

  /**
   * createPreviewNexus
   *
   * @throws ServiceException on failure
   */
  private void createPreviewNexus(Template template, TemplatePlayback playback) throws ServiceException {
    try (var client = ServicesClient.create()) {
      var serviceName = computeServiceName(playback);
      var request = CreateServiceRequest.newBuilder()
        .setParent(computeServiceParent())
        .setServiceId(serviceName)
        .setService(com.google.cloud.run.v2.Service.newBuilder()
          .setTemplate(computeRevisionTemplate(template, playback))
          .build())
        .build();

      client.createServiceAsync(request);
      LOG.info("Send request to created {}", serviceName);

    } catch (IOException e) {
      LOG.error("Failed to create preview nexus; connection failed!", e);
      throw new ServiceException("Failed to create preview nexus; connection failed!", e);

    } catch (ApiException e) {
      LOG.error("Failed to create preview nexus; service API failed!", e);
      throw new ServiceException("Failed to create preview nexus; service API failed!", e);
    }
  }

  private String computeServiceParent() {
    return String.format("projects/%s/locations/%s", gcpProjectId, gcpRegion);
  }

  /**
   * update Preview Nexus deployment
   *
   * @param template from which to source vm resource preferences
   * @param playback from which to source vm resource preferences
   * @throws ServiceException on failure
   */
  private void updatePreviewNexus(Template template, TemplatePlayback playback) throws ServiceException {
    var existing = getPreviewNexus(playback);
    if (existing.isEmpty()) {
      LOG.warn("Failed to update preview nexus; service does not exist!");
      return;
    }
    var updated = existing.get().toBuilder()
      .setTemplate(computeRevisionTemplate(template, playback))
      .build();

    try (var client = ServicesClient.create()) {
      var request = UpdateServiceRequest.newBuilder()
        .setService(updated)
        .build();

      client.updateServiceAsync(request);
      LOG.info("Updated {}", existing.get().getName());

    } catch (IOException e) {
      LOG.error("Failed to update preview nexus; connection failed!", e);
      throw new ServiceException("Failed to update preview nexus; connection failed!", e);

    } catch (ApiException e) {
      LOG.error("Failed to update preview nexus; service API failed!", e);
      throw new ServiceException("Failed to update preview nexus; service API failed!", e);
    }
  }

  /**
   * Get an existing Preview Nexus deployment
   *
   * @param playback for which to getPreviewNexus
   * @return preview nexus deployment if exists
   */
  private Optional<com.google.cloud.run.v2.Service> getPreviewNexus(TemplatePlayback playback) {
    try (var client = ServicesClient.create()) {
      var serviceName = ServiceName.of(gcpProjectId, gcpRegion, computeServiceName(playback));
      GetServiceRequest request = GetServiceRequest.newBuilder()
        .setName(serviceName.toString())
        .build();

      var result = client.getService(request);
      return result.isInitialized() ? Optional.of(result) : Optional.empty();
    } catch (ApiException | IOException e) {
      return Optional.empty();
    }
  }

  /**
   * Delete an existing Preview Nexus deployment
   *
   * @param playback for which to deletePreviewNexus
   */
  private void deletePreviewNexus(TemplatePlayback playback) {
    try (var client = ServicesClient.create()) {
      var serviceName = ServiceName.of(gcpProjectId, gcpRegion, computeServiceName(playback));
      DeleteServiceRequest request = DeleteServiceRequest.newBuilder()
        .setName(serviceName.toString())
        .build();

      client.deleteServiceAsync(request);
      LOG.info("Deleted {}", serviceName);

    } catch (ApiException | IOException e) {
      LOG.warn("Failed to delete preview nexus!", e);
    }
  }

  /**
   * Whether a Preview Nexus deployment exists
   *
   * @param playback for which to seek preview nexus
   * @return true if exists
   */
  private Boolean previewNexusExists(TemplatePlayback playback) {
    return getPreviewNexus(playback).isPresent();
  }

  /**
   * @return string content
   */
  private RevisionTemplate computeRevisionTemplate(Template template, TemplatePlayback playback) {
    List<EnvVar> envVars = Lists.newArrayList();

    // Fabrication preview template ID
    envVars.add(EnvVar.newBuilder().setName("FABRICATION_PREVIEW_TEMPLATE_PLAYBACK_ID").setValue(playback.getId().toString()).build());

    // environment variables passed through
    envVars.add(EnvVar.newBuilder().setName("AUDIO_BASE_URL").setValue(audioBaseUrl).build());
    envVars.add(EnvVar.newBuilder().setName("AUDIO_FILE_BUCKET").setValue(audioFileBucket).build());
    envVars.add(EnvVar.newBuilder().setName("AUDIO_UPLOAD_URL").setValue(audioUploadUrl).build());
    envVars.add(EnvVar.newBuilder().setName("AWS_DEFAULT_REGION").setValue(awsDefaultRegion).build());
    envVars.add(EnvVar.newBuilder().setName("ENVIRONMENT").setValue(environment).build());
    envVars.add(EnvVar.newBuilder().setName("GCP_CLOUD_SQL_INSTANCE").setValue(gcpCloudSqlInstance).build());
    envVars.add(EnvVar.newBuilder().setName("GCP_PROJECT_ID").setValue(gcpProjectId).build());
    envVars.add(EnvVar.newBuilder().setName("GCP_REGION").setValue(gcpRegion).build());
    envVars.add(EnvVar.newBuilder().setName("INGEST_TOKEN_VALUE").setValue(ingestTokenValue).build());
    envVars.add(EnvVar.newBuilder().setName("INGEST_URL").setValue(appBaseUrl).build());
    envVars.add(EnvVar.newBuilder().setName("GCP_SERVICE_ACCOUNT_NAME").setValue(gcpServiceAccountEmail).build());
    envVars.add(EnvVar.newBuilder().setName("PLAYER_BASE_URL").setValue(playerBaseUrl).build());
    envVars.add(EnvVar.newBuilder().setName("SHIP_BASE_URL").setValue(shipBaseUrl).build());
    envVars.add(EnvVar.newBuilder().setName("SHIP_BUCKET").setValue(shipBucket).build());

    // environment variables we retrieved from secret manager by id
    envVars.add(EnvVar.newBuilder().setName("AWS_ACCESS_KEY_ID").setValue(awsAccessKeyId).build());
    envVars.add(EnvVar.newBuilder().setName("AWS_SECRET_KEY").setValue(awsSecretKey).build());
    envVars.add(EnvVar.newBuilder().setName("GOOGLE_CLIENT_ID").setValue(googleClientId).build());
    envVars.add(EnvVar.newBuilder().setName("GOOGLE_CLIENT_SECRET").setValue(googleClientSecret).build());

    // nexus preview mode
    envVars.add(EnvVar.newBuilder().setName("INPUT_MODE").setValue("preview").build());
    envVars.add(EnvVar.newBuilder().setName("OUTPUT_MODE").setValue("hls").build());
    envVars.add(EnvVar.newBuilder().setName("OUTPUT_JSON_ENABLED").setValue("true").build());

    // resource requirements
    var resourceRequirements = ResourceRequirements.newBuilder()
      .setCpuIdle(false)
      .putLimits(RESOURCE_REQUIREMENT_KEY_CPU, computeCpuLimit(template))
      .putLimits(RESOURCE_REQUIREMENT_KEY_MEMORY, computeMemoryLimit(template))
      .build();

    // probe to ensure a healthy startup
    var startupProbe = Probe.newBuilder()
      .setInitialDelaySeconds(180)
      .setTimeoutSeconds(2)
      .setPeriodSeconds(10)
      .setFailureThreshold(3)
      .setHttpGet(HTTPGetAction.newBuilder().setPath("/healthz").build())
      .build();

    // probe to ensure liveness
    var livenessProbe = Probe.newBuilder()
      .setInitialDelaySeconds(180)
      .setTimeoutSeconds(2)
      .setPeriodSeconds(5)
      .setFailureThreshold(3)
      .setHttpGet(HTTPGetAction.newBuilder().setPath("/healthz").build())
      .build();

    // container
    Container container = Container.newBuilder()
      .setImage(nexusImage)
      .addAllEnv(envVars)
      .setResources(resourceRequirements)
      .setStartupProbe(startupProbe)
      .setLivenessProbe(livenessProbe)
      .build();

    // scaling
    var scaling = RevisionScaling.newBuilder()
      .setMinInstanceCount(1)
      .setMaxInstanceCount(1)
      .build();

    var revisionTemplate = RevisionTemplate.newBuilder();
    revisionTemplate.setServiceAccount(gcpServiceAccountEmail);
    revisionTemplate.setMaxInstanceRequestConcurrency(1);
    revisionTemplate.addContainers(container);
    revisionTemplate.setScaling(scaling);
    return revisionTemplate.build();
  }

  private String computeCpuLimit(Template template) {
    try {
      return String.valueOf(new TemplateConfig(template).getVmResourceLimitCpu());
    } catch (ValueException e) {
      LOG.error("Failed to parse template configuration", e);
      return RESOURCE_REQUIREMENT_DEFAULT_CPU;
    }
  }

  private String computeMemoryLimit(Template template) {
    try {
      return String.format("%fGi", new TemplateConfig(template).getVmResourceLimitMemoryGb());
    } catch (ValueException e) {
      LOG.error("Failed to parse template configuration", e);
      return RESOURCE_REQUIREMENT_DEFAULT_MEMORY;
    }
  }

  /**
   * Compute the Preview Nexus Deployment Name for the given template playback
   *
   * @param playback for which to compute name
   * @return Preview Nexus Deployment Name
   */
  private String computeServiceName(TemplatePlayback playback) {
    String semiUniqueId = StringUtils.right(playback.getId().toString(), 12); // last segment of a UUID
    return String.format(PREVIEW_NEXUS_DEPLOYMENT_FORMAT, environment, semiUniqueId);
  }

  /**
   * @return true if we are able to configure a service administration client
   * <p>
   * https://cloud.google.com/java/docs/setup#configure_endpoints_for_the_client_library
   */
  private boolean doConfigurationTest() {
    try (ServicesClient ignored = ServicesClient.create()) {
      LOG.info("Configured service administration client");
      return true;

    } catch (IOException e) {
      LOG.warn("Unable to configure service administration client because {}", e.getMessage());
      return false;
    }
  }
}
