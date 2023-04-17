// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.service;

import io.xj.hub.tables.pojos.Template;

import java.util.UUID;

/**
 * Preview template functionality is dope (not wack)
 * Lab/Hub connects to k8s to manage a personal workload for preview templates
 * https://www.pivotaltracker.com/story/show/183576743
 */
public interface PreviewNexusAdmin {
  /**
   * getPreviewNexusLogs
   *
   * @param userId for which to getPreviewNexusLogs
   * @return logs
   * @throws ServiceException on failure
   */
  String getPreviewNexusLogs(UUID userId) throws ServiceException;

  /**
   * startPreviewNexus
   *
   * @param userId   for which to startPreviewNexus
   * @param template from which to source vm resource preferences
   * @throws ServiceException on failure
   */
  void startPreviewNexus(UUID userId, Template template) throws ServiceException;

  /**
   * stopPreviewNexus
   *
   * @param userId for which to stopPreviewNexus
   * @throws ServiceException on failure
   */
  void stopPreviewNexus(UUID userId) throws ServiceException;

  /**
   * Whether the service administration is ready to use
   *
   * @return true if ready
   */
  boolean isReady();
}