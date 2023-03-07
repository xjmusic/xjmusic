// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.kubernetes;

import io.xj.hub.tables.pojos.Template;

import java.util.UUID;

/**
 * Preview template functionality is dope (not wack)
 * Lab/Hub connects to k8s to manage a personal workload for preview templates
 * https://www.pivotaltracker.com/story/show/183576743
 */
public interface KubernetesAdmin {
  /**
   * getPreviewNexusLogs
   *
   * @param userId for which to getPreviewNexusLogs
   * @return logs
   * @throws KubernetesException on failure
   */
  String getPreviewNexusLogs(UUID userId) throws KubernetesException;

  /**
   * startPreviewNexus
   *
   * @param userId   for which to startPreviewNexus
   * @param template from which to source vm resource preferences
   * @throws KubernetesException on failure
   */
  void startPreviewNexus(UUID userId, Template template) throws KubernetesException;

  /**
   * stopPreviewNexus
   *
   * @param userId for which to stopPreviewNexus
   * @throws KubernetesException on failure
   */
  void stopPreviewNexus(UUID userId) throws KubernetesException;
}
