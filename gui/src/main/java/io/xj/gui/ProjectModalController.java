// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.WindowUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;

public abstract class ProjectModalController extends ProjectController {
  private final static Logger LOG = LoggerFactory.getLogger(ProjectModalController.class);
  private final Resource fxml;

  protected ProjectModalController(
    Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    UIStateService uiStateService,
    ProjectService projectService
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.fxml = fxml;
  }

  /**
   Launches the modal.
   */
  public abstract void launchModal();

  /**
   Launches the modal.
   */
  public  void createAndShowModal(String windowName) {
    try {
      // Load the FXML file
      FXMLLoader loader = new FXMLLoader(fxml.getURL());
      loader.setControllerFactory(ac::getBean);

      // Create a new stage (window)
      Stage stage = new Stage();
      WindowUtils.setupIcon(stage);
      stage.setTitle(WindowUtils.computeTitle(windowName));

      Scene scene = new Scene(loader.load());
      themeService.setup(scene);

      // Set the scene and show the stage
      stage.setScene(scene);
      stage.initModality(Modality.APPLICATION_MODAL); // make it a modal window
      stage.initStyle(StageStyle.UTILITY);
      onStageReady();
      stage.showAndWait();

    } catch (IOException e) {
      LOG.error("Failed to launch modal", e);
    }
  }
}
