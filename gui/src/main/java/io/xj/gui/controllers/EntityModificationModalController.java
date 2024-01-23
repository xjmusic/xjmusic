// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers;

import io.xj.gui.services.ThemeService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class EntityModificationModalController extends ReadyAfterBootModalController {
  static final Logger LOG = LoggerFactory.getLogger(EntityModificationModalController.class);
  private StringProperty windowTitle = new SimpleStringProperty(); // TODO set this when launching the modal

  @FXML
  protected VBox container;

  @FXML
  protected TextField fieldName;

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

  /**
   Create a new Template.
   */
  public void createTemplate() {
    LOG.info("Will create Template");
  }

  /**
   Create a new Library.
   */
  public void createLibrary() {
    LOG.info("Will create Library");
  }

  /**
   Create a new Program.
   */
  public void createProgram() {
    LOG.info("Will create Program");
  }

  /**
   Create a new Instrument.
   */
  public void createInstrument() {
    LOG.info("Will create Instrument");
  }

}
