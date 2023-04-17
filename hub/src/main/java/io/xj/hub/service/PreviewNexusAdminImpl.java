// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.service;

import io.xj.hub.tables.pojos.Template;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Preview template functionality is dope (not wack)
 * Lab/Hub connects to k8s to manage a personal workload for preview templates
 * https://www.pivotaltracker.com/story/show/183576743
 */
@Service
public class PreviewNexusAdminImpl implements PreviewNexusAdmin {
  public PreviewNexusAdminImpl() {
    // no op
  }

  @Override
  public String getPreviewNexusLogs(UUID userId) {
    return "";
  }

  @Override
  public void startPreviewNexus(UUID userId, Template template) {
    // no op
  }

  @Override
  public void stopPreviewNexus(UUID userId) {
    // no op
  }

  @Override
  public boolean isReady() {
    return true;
  }

}
