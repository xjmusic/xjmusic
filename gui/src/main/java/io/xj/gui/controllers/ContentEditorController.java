// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ProjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ContentEditorController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(ContentEditorController.class);
  private final ProjectService projectService;

  public ContentEditorController(
    ProjectService projectService
  ) {
    this.projectService = projectService;
  }

  @Override
  public void onStageReady() {
    // TODO: on stage ready
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }
}
