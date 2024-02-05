// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui;

import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ReadyAfterBoot;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Region;
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


  /**
   Show a custom confirmation dialog with Yes/No options.

   @param title   title of the dialog
   @param header  header of the dialog
   @param content content of the dialog
   @return true if the user clicked 'Yes', false otherwise
   */
  @SuppressWarnings({"SameParameterValue", "BooleanMethodIsAlwaysInverted"})
  protected boolean showConfirmationDialog(String title, String header, String content) {
    // Create a custom dialog
    Dialog<ButtonType> dialog = new Dialog<>();
    themeService.setup(dialog);
    dialog.setTitle(title);

    // Set the header and content
    DialogPane dialogPane = dialog.getDialogPane();
    dialogPane.setHeaderText(header);
    dialogPane.setContentText(content);

    // Add Yes and No buttons
    ButtonType yesButton = new ButtonType("Yes", ButtonType.OK.getButtonData());
    ButtonType noButton = new ButtonType("No", ButtonType.CANCEL.getButtonData());
    dialogPane.getButtonTypes().addAll(yesButton, noButton);

    // Ensure it's resizable and has a preferred width
    dialogPane.setMinHeight(Region.USE_PREF_SIZE);
    dialogPane.setPrefWidth(400); // You can adjust this value

    // Show the dialog and wait for the user to close it
    java.util.Optional<ButtonType> result = dialog.showAndWait();

    // Return true if 'Yes' was clicked, false otherwise
    return result.isPresent() && result.get() == yesButton;
  }
}
