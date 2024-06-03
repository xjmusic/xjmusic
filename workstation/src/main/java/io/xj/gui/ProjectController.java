// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ReadyAfterBoot;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

public abstract class ProjectController implements ReadyAfterBoot {
  protected final ApplicationContext ac;
  protected final ThemeService themeService;
  protected final UIStateService uiStateService;
  protected final ProjectService projectService;
  protected final Resource fxml;

  /**
   Common constructor for all project controllers

   @param fxml           FXML resource
   @param ac             application context
   @param themeService   common theme service
   @param uiStateService common UI state service
   @param projectService common project service
   */
  protected ProjectController(
    Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    UIStateService uiStateService,
    ProjectService projectService
  ) {
    this.ac = ac;
    this.themeService = themeService;
    this.fxml = fxml;
    this.uiStateService = uiStateService;
    this.projectService = projectService;
  }
}
