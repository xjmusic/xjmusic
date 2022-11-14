// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.kubernetes;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.util.Config;
import io.kubernetes.client.util.Yaml;
import io.xj.hub.TemplateConfig;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.Environment;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.lib.util.Values.MILLIS_PER_SECOND;

/**
 Preview template functionality is dope (not wack)
 Lab/Hub connects to k8s to manage a personal workload for preview templates
 https://www.pivotaltracker.com/story/show/183576743
 */
@Singleton
public class KubernetesAdminImpl implements KubernetesAdmin {
  private static final String PREVIEW_NEXUS_DEPLOYMENT_FORMAT = "nexus-preview-%s";
  private static final String LABEL_K8S_APP_KEY = "k8s-app";
  private static final String SERVICE_TEMPLATE_YAML_PATH = "kubernetes/lab-nexus-preview-template.yaml";
  private static final String RESOURCE_REQUIREMENT_KEY_CPU = "cpu";
  private static final String RESOURCE_REQUIREMENT_KEY_MEMORY = "memory";
  private static final String LOG_LINE_FILTER_PREFIX = "[main] ";
  private static final String LOG_LINE_REMOVE = " i.x.n.w.NexusWorkImpl ";
  private final Logger LOG = LoggerFactory.getLogger(KubernetesAdminImpl.class);
  private final String labNamespace;
  private final int logTailLines;
  private final int clientConfigExpirySeconds;
  private boolean isConfigured;
  private long lastConfiguredMillis;

  @Inject
  public KubernetesAdminImpl(Environment env) {
    labNamespace = env.getKubernetesNamespaceLab();
    logTailLines = env.getKubernetesLogTailLines();
    clientConfigExpirySeconds = env.getKubernetesClientConfigExpirySeconds();

    isConfigured = _buildAndSetDefaultApiClient();
  }

  @Override
  public String getPreviewNexusLogs(UUID userId) {
    if (!buildAndSetDefaultApiClient())
      return "Kubernetes client is not configured!";

    var name = computePreviewNexusDeploymentName(userId);

    try {
      var api = new CoreV1Api();
      var pods = api.listNamespacedPod(labNamespace, null, null, null, null, String.format("%s=%s", LABEL_K8S_APP_KEY, name), null, null, null, null, null);
      if (pods.getItems().isEmpty()) {
        return String.format("Kubernetes deployment %s not found!", name);
      }
      var containerName = Objects.requireNonNull(pods.getItems().get(0).getMetadata()).getName();
      var log = api.readNamespacedPodLog(containerName, labNamespace, name, false, false, null, null, null, null, logTailLines * 2, null);
      var lines = List.of(Text.splitLines(log));
      return lines.stream()
        .filter((L) -> Text.beginsWith(L, LOG_LINE_FILTER_PREFIX))
        .map((L) -> L.substring(LOG_LINE_FILTER_PREFIX.length()))
        .map((L) -> L.replace(LOG_LINE_REMOVE, ""))
        .limit(logTailLines)
        .collect(Collectors.joining("\n"));

    } catch (ApiException e) {
      return String.format("Failed to get logs for %s", name);
    }
  }

  @Override
  public void startPreviewNexus(UUID userId, Template template) throws KubernetesException {
    if (!buildAndSetDefaultApiClient()) {
      LOG.warn("Kubernetes client is not configured!");
      return;
    }
    if (!previewNexusExists(userId)) createPreviewNexus(userId);
    updatePreviewNexus(userId, 0, template);
    updatePreviewNexus(userId, 1, template);
  }

  @Override
  public void stopPreviewNexus(UUID userId) throws KubernetesException {
    if (!buildAndSetDefaultApiClient()) {
      LOG.warn("Kubernetes client is not configured!");
      return;
    }
    if (!previewNexusExists(userId)) return;
    updatePreviewNexus(userId, 0, null);
  }

  /**
   Checks if the configuration needs to be reconfigured (based on expiry seconds) and return true if it is OK

   @return true if configured
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean buildAndSetDefaultApiClient() {
    if (lastConfiguredMillis + clientConfigExpirySeconds * MILLIS_PER_SECOND < System.currentTimeMillis())
      isConfigured = _buildAndSetDefaultApiClient();
    return isConfigured;
  }

  /**
   Build and set the Kubernetes client
   */
  private boolean _buildAndSetDefaultApiClient() {
    lastConfiguredMillis = System.currentTimeMillis();
    ApiClient client;

    try {
      client = Config.fromCluster();
      Configuration.setDefaultApiClient(client);
      LOG.info("Kubernetes client configured from cluster; authenticated via {}", String.join(", ", client.getAuthentications().keySet()));
      return true;
    } catch (Exception ignored) {
      LOG.warn("Kubernetes client could not be configured from cluster!");
    }

    try {
      client = Config.defaultClient();
      Configuration.setDefaultApiClient(client);
      LOG.info("Configured default Kubernetes client: {}", String.join(", ", client.getAuthentications().keySet()));
      return true;
    } catch (Exception e) {
      LOG.warn("Kubernetes default client could not be configured!");
    }

    LOG.warn("Kubernetes client is not configured.");
    return false;
  }

  /**
   createPreviewNexus

   @param userId for which to createPreviewNexus
   @throws KubernetesException on failure
   */
  private void createPreviewNexus(UUID userId) throws KubernetesException {
    try {
      var name = computePreviewNexusDeploymentName(userId);
      LOG.info("Kubernetes will create deployment {}", name);

      (new AppsV1Api()).createNamespacedDeployment(labNamespace, computePreviewNexusDeployment(userId, 0, null), null, null, null, null);

    } catch (IOException e) {
      LOG.error("Failed to connect to Kubernetes API!", e);
      throw new KubernetesException("Failed to connect to Kubernetes API!", e);

    } catch (ApiException e) {
      LOG.error("Failed to create preview nexus via Kubernetes! {} {}", e.getResponseHeaders(), e.getResponseBody());
      throw new KubernetesException(String.format("Failed to create preview nexus via Kubernetes! %s %s", e.getResponseHeaders(), e.getResponseBody()));
    }
  }

  /**
   update Preview Nexus deployment

   @param userId   for which to updatePreviewNexus
   @param replicas in pod
   @param template from which to source vm resource preferences
   @throws KubernetesException on failure
   */
  private void updatePreviewNexus(UUID userId, int replicas, @Nullable Template template) throws KubernetesException {
    try {
      var name = computePreviewNexusDeploymentName(userId);
      LOG.info("Kubernetes will scale deployment {} to {} replicas", name, replicas);

      (new AppsV1Api()).replaceNamespacedDeployment(name, labNamespace, computePreviewNexusDeployment(userId, replicas, template), null, null, null, null);

    } catch (IOException e) {
      LOG.error("Failed to connect to Kubernetes API!", e);
      throw new KubernetesException("Failed to connect to Kubernetes API!", e);

    } catch (ApiException e) {
      LOG.error("Failed to scale preview nexus via Kubernetes! Code {}: {}", e.getResponseHeaders(), e.getResponseBody());
      throw new KubernetesException(String.format("Failed to scale preview nexus via Kubernetes! Code %s: %s", e.getResponseHeaders(), e.getResponseBody()));
    }
  }

  /**
   previewNexusExists

   @param userId for which to previewNexusExists
   @return true if exists
   */
  private Boolean previewNexusExists(UUID userId) {
    try {
      var name = computePreviewNexusDeploymentName(userId);
      var api = new AppsV1Api();
      api.readNamespacedDeployment(name, labNamespace, null);
      return true;

    } catch (ApiException e) {
      return false;
    }
  }

  /**
   Compute the deployment template by reading and amending the baseline YAML spec from internal resources

   @param userId   for which to compute a deployment
   @param replicas in pod
   @param template from which to deploy
   @return Kubernetes deployment
   @throws IOException         on I/O failure
   @throws KubernetesException on API failure
   */
  private V1Deployment computePreviewNexusDeployment(UUID userId, int replicas, @Nullable Template template) throws IOException, KubernetesException {
    var name = computePreviewNexusDeploymentName(userId);
    V1Deployment deployment = (V1Deployment) Yaml.load(readServiceTemplateYamlContent());

    // metadata for deployment and spec template
    V1ObjectMeta metadata = new V1ObjectMeta();
    metadata.setNamespace(labNamespace);
    metadata.setName(name);
    metadata.setLabels(ImmutableMap.of(LABEL_K8S_APP_KEY, name));

    // deployment metadata
    deployment.setMetadata(metadata);

    // spec
    Objects.requireNonNull(deployment.getSpec());
    deployment.getSpec().setReplicas(replicas);
    V1LabelSelector labelSelector = new V1LabelSelector();
    labelSelector.setMatchLabels(ImmutableMap.of(LABEL_K8S_APP_KEY, name));
    deployment.getSpec().setSelector(labelSelector);

    // template metadata
    Objects.requireNonNull(deployment.getSpec().getTemplate());
    deployment.getSpec().getTemplate().setMetadata(metadata);

    // template spec container
    Objects.requireNonNull(deployment.getSpec().getTemplate().getSpec());
    var container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
    container.setName(name);

    V1EnvVar userIdEnvVar = new V1EnvVar();
    userIdEnvVar.setName(Environment.FABRICATION_PREVIEW_USER_ID);
    userIdEnvVar.setValue(userId.toString());
    container.setEnv(List.of(userIdEnvVar));
    if (Objects.nonNull(template)) try {
      var templateConfig = new TemplateConfig(template);
      V1ResourceRequirements resources = new V1ResourceRequirements();
      resources.setLimits(ImmutableMap.of(RESOURCE_REQUIREMENT_KEY_CPU, Quantity.fromString(String.valueOf(templateConfig.getVmResourceLimitCpu()))));
      resources.setLimits(ImmutableMap.of(RESOURCE_REQUIREMENT_KEY_MEMORY, Quantity.fromString(String.format("%fG", templateConfig.getVmResourceLimitMemoryGb()))));
      resources.setRequests(ImmutableMap.of(RESOURCE_REQUIREMENT_KEY_CPU, Quantity.fromString(String.valueOf(templateConfig.getVmResourceRequestCpu()))));
      resources.setRequests(ImmutableMap.of(RESOURCE_REQUIREMENT_KEY_MEMORY, Quantity.fromString(String.format("%fG", templateConfig.getVmResourceRequestMemoryGb()))));
      container.setResources(resources);
    } catch (ValueException e) {
      throw new KubernetesException(String.format("Invalid VM settings in template config: %s", e.getMessage()), e);
    }
    deployment.getSpec().getTemplate().getSpec().getContainers().set(0, container);

    return deployment;
  }

  /**
   Read the Kubernetes service template YAML from internal resources

   @return string content
   @throws IOException on failure
   */
  private String readServiceTemplateYamlContent() throws IOException {
    return new String(new BufferedInputStream(Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(SERVICE_TEMPLATE_YAML_PATH))).readAllBytes());
  }

  /**
   Compute the Preview Nexus Deployment Name for the given user

   @param userId for which to compute name
   @return Preview Nexus Deployment Name
   */
  private String computePreviewNexusDeploymentName(UUID userId) {
    return String.format(PREVIEW_NEXUS_DEPLOYMENT_FORMAT, userId);
  }
}
