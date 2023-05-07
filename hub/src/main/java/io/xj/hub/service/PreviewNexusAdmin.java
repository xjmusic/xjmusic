// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.service;

import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplatePlayback;

/**
 * Preview template functionality is dope (not wack)
 * Lab/Hub connects to k8s to manage a personal workload for preview templates
 * https://www.pivotaltracker.com/story/show/183576743
 */
public interface PreviewNexusAdmin {
  /**
   * getPreviewNexusLogs
   *
   * @param playback for which to getPreviewNexusLogs
   * @return logs
   * @throws ServiceException on failure
   */
  String getPreviewNexusLogs(TemplatePlayback playback) throws ServiceException;

  /**
   * startPreviewNexus
   *
   * @param template from which to source vm resource preferences
   * @param playback from which to source vm resource preferences
   * @throws ServiceException on failure
   */
  void startPreviewNexus(Template template, TemplatePlayback playback) throws ServiceException;

  /**
   * stopPreviewNexus
   *
   * @param playback for which to stopPreviewNexus
   * @throws ServiceException on failure
   */
  void stopPreviewNexus(TemplatePlayback playback) throws ServiceException;

  /**
   * Whether the service administration is ready to use
   *
   * @return true if ready
   */
  boolean isReady();
}
