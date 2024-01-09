// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.impl;

import io.xj.gui.WorkstationIcon;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.controllers.ReadyAfterBootModalController;
import io.xj.gui.services.ThemeService;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public abstract class ReadyAfterBootModalControllerImpl implements ReadyAfterBootModalController {
  private final static Logger LOG = LoggerFactory.getLogger(ReadyAfterBootModalControllerImpl.class);

  /**
   Launches the modal.
   */
  protected void doLaunchModal(ApplicationContext ac, ThemeService themeService, Resource fxml, String windowName) {
    try {
      // Load the FXML file
      FXMLLoader loader = new FXMLLoader(fxml.getURL());
      loader.setControllerFactory(ac::getBean);

      // Create a new stage (window)
      Stage stage = new Stage();
      WorkstationIcon.setup(stage, windowName);

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
