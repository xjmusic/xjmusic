// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.ProjectController;
import io.xj.gui.controllers.content.ContentBrowserController;
import io.xj.gui.controllers.content.LibraryEditorController;
import io.xj.gui.controllers.content.instrument.InstrumentAudioEditorController;
import io.xj.gui.controllers.content.instrument.InstrumentEditorController;
import io.xj.gui.controllers.content.program.ProgramEditorController;
import io.xj.gui.controllers.fabrication.FabricationTimelineController;
import io.xj.gui.controllers.template.TemplateBrowserController;
import io.xj.gui.controllers.template.TemplateEditorController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.model.util.StringUtils;
import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class MainController extends ProjectController {
  static final Logger LOG = LoggerFactory.getLogger(MainController.class);
  private final FabricationTimelineController fabricationTimelineController;
  private final MainMenuController mainMenuController;
  private final MainPaneBottomController mainPaneBottomController;
  private final MainPaneTopController mainPaneTopController;
  private final ContentBrowserController contentBrowserController;
  private final LibraryEditorController libraryEditorController;
  private final ProgramEditorController programEditorController;
  private final InstrumentEditorController instrumentEditorController;
  private final InstrumentAudioEditorController instrumentAudioEditorController;
  private final TemplateEditorController templateEditorController;
  private final TemplateBrowserController templateBrowserController;

  @FXML
  ImageView startupContainer;

  public MainController(
    @Value("classpath:/views/main.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    ContentBrowserController contentBrowserController,
    FabricationTimelineController fabricationTimelineController,
    InstrumentEditorController instrumentEditorController,
    LibraryEditorController libraryEditorController,
    MainMenuController mainMenuController,
    MainPaneBottomController mainPaneBottomController,
    MainPaneTopController mainPaneTopController,
    ProgramEditorController programEditorController,
    ProjectService projectService,
    TemplateBrowserController templateBrowserController,
    TemplateEditorController templateEditorController,
    UIStateService uiStateService,
    InstrumentAudioEditorController instrumentAudioEditorController
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.contentBrowserController = contentBrowserController;
    this.fabricationTimelineController = fabricationTimelineController;
    this.instrumentEditorController = instrumentEditorController;
    this.libraryEditorController = libraryEditorController;
    this.mainMenuController = mainMenuController;
    this.mainPaneBottomController = mainPaneBottomController;
    this.mainPaneTopController = mainPaneTopController;
    this.programEditorController = programEditorController;
    this.templateBrowserController = templateBrowserController;
    this.templateEditorController = templateEditorController;
    this.instrumentAudioEditorController = instrumentAudioEditorController;
  }

  @Override
  public void onStageReady() {
    try {
      contentBrowserController.onStageReady();
      fabricationTimelineController.onStageReady();
      instrumentEditorController.onStageReady();
      instrumentAudioEditorController.onStageReady();
      libraryEditorController.onStageReady();
      mainMenuController.onStageReady();
      mainPaneBottomController.onStageReady();
      mainPaneTopController.onStageReady();
      programEditorController.onStageReady();
      templateBrowserController.onStageReady();
      templateEditorController.onStageReady();
      uiStateService.onStageReady();

      startupContainer.visibleProperty().bind(projectService.isStateStandbyProperty());
      startupContainer.managedProperty().bind(projectService.isStateStandbyProperty());

    } catch (Exception e) {
      LOG.error("Error initializing main controller! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

  @Override
  public void onStageClose() {
    contentBrowserController.onStageClose();
    fabricationTimelineController.onStageClose();
    instrumentEditorController.onStageClose();
    instrumentAudioEditorController.onStageClose();
    libraryEditorController.onStageClose();
    mainMenuController.onStageClose();
    mainPaneBottomController.onStageClose();
    mainPaneTopController.onStageClose();
    programEditorController.onStageClose();
    templateBrowserController.onStageClose();
    templateEditorController.onStageClose();
    uiStateService.onStageClose();
  }
}
