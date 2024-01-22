// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ThemeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class EntityModificationModalController extends ReadyAfterBootModalController {
  private StringProperty windowTitle = new SimpleStringProperty(); // TODO set this when launching the modal

  @FXML
  protected VBox container;

  @FXML
  protected TextField fieldEntityName;

  public EntityModificationModalController(
    @Value("classpath:/views/entity-modification-modal.fxml") Resource fxml,
    ConfigurableApplicationContext ac,
    ThemeService themeService
    ) {
    super(ac, themeService, fxml);
  }

  @Override
  public void onStageReady() {
    // todo implement entity modification modal
  }

  @Override
  public void onStageClose() {
    // no op
  }

  @Override
  public void launchModal() {
    createAndShowModal(windowTitle.get());
  }
}
