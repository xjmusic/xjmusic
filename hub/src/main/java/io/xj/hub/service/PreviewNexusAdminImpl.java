// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.service;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Strings;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.logging.LogEntry;
import com.google.cloud.logging.Logging;
import com.google.cloud.logging.Logging.EntryListOption;
import com.google.cloud.logging.LoggingOptions;
import com.google.cloud.run.v2.Container;
import com.google.cloud.run.v2.ContainerPort;
import com.google.cloud.run.v2.CreateServiceRequest;
import com.google.cloud.run.v2.DeleteServiceRequest;
import com.google.cloud.run.v2.EnvVar;
import com.google.cloud.run.v2.GetServiceRequest;
import com.google.cloud.run.v2.ResourceRequirements;
import com.google.cloud.run.v2.RevisionTemplate;
import com.google.cloud.run.v2.ServiceName;
import com.google.cloud.run.v2.ServicesClient;
import com.google.cloud.run.v2.UpdateServiceRequest;
import io.xj.hub.TemplateConfig;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

/**
 * Preview template functionality is dope (not wack)
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
  private static final String LOG_LINE_FILTER_BEGINS_WITH = "[main] ";
  private static final String LOG_LINE_REMOVE = " i.x.n.w.NexusWorkImpl ";
  private final Logger LOG = LoggerFactory.getLogger(PreviewNexusAdminImpl.class);
  private final String gcpServiceAccountEmail;
  private final boolean isConfigured;
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
  private final String ingestUrl;
  private final String playerBaseUrl;
  private final String postgresDatabase;
  private final String postgresPass;
  private final String postgresUser;
  private final String shipBaseUrl;
  private final String shipBucket;

  public PreviewNexusAdminImpl(AppEnvironment env) {
    String envSecretRefName = env.getServiceContainerEnvSecretRefName();
    String namespace = env.getServiceNamespace();
    logTailLines = env.getServiceLogTailLines();
    nexusImage = env.getGcpServiceNexusImage();
    gcpServiceAccountEmail = env.getGcpServiceAccountEmail();
    gcpProjectId = env.getGcpProjectId();
    gcpRegion = env.getGcpRegion();
    appBaseUrl = env.getAppBaseUrl();
    audioBaseUrl = env.getAudioBaseUrl();
    audioFileBucket = env.getAudioFileBucket();
    audioUploadUrl = env.getAudioUploadURL();
    awsAccessKeyId = env.getAwsAccessKeyID();
    awsSecretKey = env.getAwsSecretKey();
    environment = env.getPlatformEnvironment();
    gcpCloudSqlInstance = env.getGcpCloudSqlInstance();
    googleClientId = env.getGoogleClientID();
    googleClientSecret = env.getGoogleClientSecret();
    ingestTokenValue = env.getIngestTokenValue();
    ingestUrl = env.getIngestURL();
    playerBaseUrl = env.getPlayerBaseUrl();
    postgresDatabase = env.getPostgresDatabase();
    postgresPass = env.getPostgresPass();
    postgresUser = env.getPostgresUser();
    shipBaseUrl = env.getShipBaseUrl();
    shipBucket = env.getShipBucket();
    awsDefaultRegion = env.getAwsDefaultRegion();

    isConfigured = doConfigurationTest();
    LOG.info("Service administrator will create containers namespace={} with secretRef={}", namespace, envSecretRefName);
  }

  @Override
  public String getPreviewNexusLogs(UUID userId) {
    if (!isConfigured)
      return "Service administrator is not configured!";

    try {
      var serviceId = computeServiceName(userId);

      List<String> lines = Lists.newArrayList();

      Instant now = Instant.now();
      Instant before = now.minus(Duration.ofMinutes(1));

      String filter = String.format("resource.type=\"cloud_run_revision\""
          + " AND resource.labels.service_name=\"%s\""
          + " AND timestamp >= \"%s\""
          + " AND timestamp <= \"%s\"",
        serviceId, before.toString(), now);

      Logging logging = LoggingOptions.getDefaultInstance().getService();
      EntryListOption entryListOption = EntryListOption.filter(filter);
      var entries = logging.listLogEntries(entryListOption);

      for (LogEntry logEntry : entries.iterateAll()) {
        if (Text.beginsWith(logEntry.getPayload().getData().toString(), LOG_LINE_FILTER_BEGINS_WITH)) {
          lines.add(logEntry.getPayload().getData().toString());
        }
      }

      return String.join("\n", Values.last(logTailLines, lines).stream()
        .map((L) -> L.substring(LOG_LINE_FILTER_BEGINS_WITH.length()))
        .map((L) -> L.replace(LOG_LINE_REMOVE, "")).toList());
    } catch (RuntimeException e) {
      LOG.error("Failed to get logs for preview nexus", e);
      return String.format("Failed to get logs for preview nexus: %s", e.getMessage());
    }
  }

  @Override
  public void startPreviewNexus(UUID userId, Template template) throws ServiceException {
    if (!isConfigured) {
      LOG.warn("Service administrator is not configured!");
      return;
    }
    if (!previewNexusExists(userId)) {
      createPreviewNexus(userId, template);
    } else {
      updatePreviewNexus(userId, template);
    }
  }

  @Override
  public void stopPreviewNexus(UUID userId) {
    if (!isConfigured) {
      LOG.warn("Service administrator is not configured!");
      return;
    }
    if (!previewNexusExists(userId)) return;
    deletePreviewNexus(userId);
  }

  @Override
  public boolean isReady() {
    return isConfigured;
  }

  /**
   * createPreviewNexus
   *
   * @param userId for which to createPreviewNexus
   * @throws ServiceException on failure
   */
  private void createPreviewNexus(UUID userId, Template template) throws ServiceException {
    try (var client = ServicesClient.create()) {
      var serviceName = computeServiceName(userId);
      var request = CreateServiceRequest.newBuilder()
        .setParent(computeServiceParent())
        .setServiceId(serviceName)
        .setService(com.google.cloud.run.v2.Service.newBuilder()
          .setTemplate(computeRevisionTemplate(template))
          .build())
        .build();

      var result = client.createServiceAsync(request).get();
      LOG.info("Created {}", result.getName());

    } catch (IOException e) {
      LOG.error("Failed to create preview nexus; connection failed!", e);
      throw new ServiceException("Failed to create preview nexus; connection failed!", e);

    } catch (ApiException e) {
      LOG.error("Failed to create preview nexus; service API failed!", e);
      throw new ServiceException("Failed to create preview nexus; service API failed!", e);

    } catch (ExecutionException e) {
      LOG.error("Failed to create preview nexus; execution failed!", e);
      throw new ServiceException("Failed to create preview nexus; service API failed!", e);

    } catch (InterruptedException e) {
      LOG.error("Failed to create preview nexus; execution interrupted!", e);
      throw new ServiceException("Failed to create preview nexus; service API failed!", e);
    }
  }

  private String computeServiceParent() {
    return String.format("projects/%s/locations/%s", gcpProjectId, gcpRegion);
  }

  /**
   * update Preview Nexus deployment
   *
   * @param userId   for which to updatePreviewNexus
   * @param template from which to source vm resource preferences
   * @throws ServiceException on failure
   */
  private void updatePreviewNexus(UUID userId, Template template) throws ServiceException {
    var existing = getPreviewNexus(userId);
    if (existing.isEmpty()) {
      LOG.warn("Failed to update preview nexus; service does not exist!");
      return;
    }
    var updated = existing.get().toBuilder()
      .setTemplate(computeRevisionTemplate(template))
      .build();

    try (var client = ServicesClient.create()) {
      var request = UpdateServiceRequest.newBuilder()
        .setService(updated)
        .build();

      var result = client.updateServiceAsync(request).get();
      LOG.info("Updated {}", result.getName());

    } catch (IOException e) {
      LOG.error("Failed to update preview nexus; connection failed!", e);
      throw new ServiceException("Failed to update preview nexus; connection failed!", e);

    } catch (ApiException e) {
      LOG.error("Failed to update preview nexus; service API failed!", e);
      throw new ServiceException("Failed to update preview nexus; service API failed!", e);

    } catch (ExecutionException e) {
      LOG.error("Failed to update preview nexus; execution failed!", e);
      throw new ServiceException("Failed to update preview nexus; service API failed!", e);

    } catch (InterruptedException e) {
      LOG.error("Failed to update preview nexus; execution interrupted!", e);
      throw new ServiceException("Failed to update preview nexus; service API failed!", e);
    }
  }

  /**
   * Get an existing Preview Nexus deployment
   *
   * @param userId for which to getPreviewNexus
   * @return preview nexus deployment if exists
   */
  private Optional<com.google.cloud.run.v2.Service> getPreviewNexus(UUID userId) {
    try (var client = ServicesClient.create()) {
      var serviceName = ServiceName.of(gcpProjectId, gcpRegion, computeServiceName(userId));
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
   * @param userId for which to deletePreviewNexus
   */
  private void deletePreviewNexus(UUID userId) {
    try (var client = ServicesClient.create()) {
      var serviceName = ServiceName.of(gcpProjectId, gcpRegion, computeServiceName(userId));
      DeleteServiceRequest request = DeleteServiceRequest.newBuilder()
        .setName(serviceName.toString())
        .build();

      // block until deleted
      client.deleteServiceAsync(request).get();
      LOG.info("Deleted {}", serviceName);

    } catch (ApiException | IOException | InterruptedException | ExecutionException e) {
      LOG.warn("Failed to delete preview nexus!", e);
    }
  }

  /**
   * previewNexusExists
   *
   * @param userId for which to previewNexusExists
   * @return true if exists
   */
  private Boolean previewNexusExists(UUID userId) {
    return getPreviewNexus(userId).isPresent();
  }

  /**
   * @return string content
   */
  private RevisionTemplate computeRevisionTemplate(Template template) {
    List<EnvVar> envVars = Lists.newArrayList();

    // Ship key from template
    if (Strings.isNullOrEmpty(template.getShipKey())) {
      throw new IllegalArgumentException("Template must have a ship key!");
    }
    envVars.add(EnvVar.newBuilder().setName("SHIP_KEY").setValue(template.getShipKey()).build());

    // environment variables passed through
    envVars.add(EnvVar.newBuilder().setName("APP_BASE_URL").setValue(appBaseUrl).build());
    envVars.add(EnvVar.newBuilder().setName("AUDIO_BASE_URL").setValue(audioBaseUrl).build());
    envVars.add(EnvVar.newBuilder().setName("AUDIO_FILE_BUCKET").setValue(audioFileBucket).build());
    envVars.add(EnvVar.newBuilder().setName("AUDIO_UPLOAD_URL").setValue(audioUploadUrl).build());
    envVars.add(EnvVar.newBuilder().setName("AWS_DEFAULT_REGION").setValue(awsDefaultRegion).build());
    envVars.add(EnvVar.newBuilder().setName("ENVIRONMENT").setValue(environment).build());
    envVars.add(EnvVar.newBuilder().setName("GCP_CLOUD_SQL_INSTANCE").setValue(gcpCloudSqlInstance).build());
    envVars.add(EnvVar.newBuilder().setName("GCP_PROJECT_ID").setValue(gcpProjectId).build());
    envVars.add(EnvVar.newBuilder().setName("GCP_REGION").setValue(gcpRegion).build());
    envVars.add(EnvVar.newBuilder().setName("INGEST_TOKEN_VALUE").setValue(ingestTokenValue).build());
    envVars.add(EnvVar.newBuilder().setName("INGEST_URL").setValue(ingestUrl).build());
    envVars.add(EnvVar.newBuilder().setName("GCP_SERVICE_ACCOUNT_NAME").setValue(gcpServiceAccountEmail).build());
    envVars.add(EnvVar.newBuilder().setName("PLAYER_BASE_URL").setValue(playerBaseUrl).build());
    envVars.add(EnvVar.newBuilder().setName("POSTGRES_DATABASE").setValue(postgresDatabase).build());
    envVars.add(EnvVar.newBuilder().setName("SHIP_BASE_URL").setValue(shipBaseUrl).build());
    envVars.add(EnvVar.newBuilder().setName("SHIP_BUCKET").setValue(shipBucket).build());

    // environment variables we retrieved from secret manager by id
    envVars.add(EnvVar.newBuilder().setName("AWS_ACCESS_KEY_ID").setValue(awsAccessKeyId).build());
    envVars.add(EnvVar.newBuilder().setName("AWS_SECRET_KEY").setValue(awsSecretKey).build());
    envVars.add(EnvVar.newBuilder().setName("GOOGLE_CLIENT_ID").setValue(googleClientId).build());
    envVars.add(EnvVar.newBuilder().setName("GOOGLE_CLIENT_SECRET").setValue(googleClientSecret).build());
    envVars.add(EnvVar.newBuilder().setName("POSTGRES_PASS").setValue(postgresPass).build());
    envVars.add(EnvVar.newBuilder().setName("POSTGRES_USER").setValue(postgresUser).build());

/*
    // probe to ensure a healthy startup
    var startupProbe = Probe.newBuilder()
      .setInitialDelaySeconds(60)
      .setTimeoutSeconds(2)
      .setPeriodSeconds(7)
      .setFailureThreshold(3)
      .setTcpSocket(TCPSocketAction.newBuilder().setPort(8080).build())
      .setHttpGet(HTTPGetAction.newBuilder().setPath("/healthz").build())
      .build();

    // probe to ensure liveness
    var livenessProbe = Probe.newBuilder()
      .setTimeoutSeconds(2)
      .setHttpGet(HTTPGetAction.newBuilder().setPath("/healthz").build())
      .build();
*/

    // resource requirements
    var resourceRequirements = ResourceRequirements.newBuilder()
      .setCpuIdle(false)
      .putLimits(RESOURCE_REQUIREMENT_KEY_CPU, computeCpuLimit(template))
      .putLimits(RESOURCE_REQUIREMENT_KEY_MEMORY, computeMemoryLimit(template))
      .build();

    // container
    Container container = Container.newBuilder()
      .setImage(nexusImage)
      .addAllEnv(envVars)
      .addPorts(ContainerPort.newBuilder().setContainerPort(8080).build())
//      .setStartupProbe(startupProbe)
//      .setLivenessProbe(livenessProbe)
      .setResources(resourceRequirements)
      .build();

    // annotations
    Map<String, String> annotations = new HashMap<>();
    annotations.put("autoscaling.knative.dev/minScale", Integer.toString(1));
    annotations.put("autoscaling.knative.dev/maxScale", Integer.toString(1));
    annotations.put("run.googleapis.com/cpu-throttling", "false");

    var revisionTemplate = RevisionTemplate.newBuilder();
    revisionTemplate.putAllAnnotations(annotations);
    revisionTemplate.setServiceAccount(gcpServiceAccountEmail);
    revisionTemplate.addContainers(container);
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
   * Compute the Preview Nexus Deployment Name for the given user
   *
   * @param userId for which to compute name
   * @return Preview Nexus Deployment Name
   */
  private String computeServiceName(UUID userId) {
    String semiUniqueId = StringUtils.right(userId.toString(), 12); // last segment of a UUID
    return String.format(PREVIEW_NEXUS_DEPLOYMENT_FORMAT, environment, semiUniqueId);
  }

  /**
   * @return true if we are able to configure a service administration client
   * <p>
   * // https://cloud.google.com/java/docs/setup#configure_endpoints_for_the_client_library
   */
  private boolean doConfigurationTest() {
    try (ServicesClient ignored = ServicesClient.create()) {
      computeServiceName(UUID.randomUUID());
      LOG.info("Configured service administration client");
      return true;

    } catch (IOException e) {
      LOG.error("Unable to configure service administration client", e);
      return false;
    }
  }
}
