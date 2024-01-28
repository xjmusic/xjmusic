// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.instrument;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.controllers.ReadyAfterBootController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.UIStateService;
import io.xj.hub.tables.pojos.InstrumentAudio;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class InstrumentAudioEditorController extends BrowserController implements ReadyAfterBootController {
  static final Logger LOG = LoggerFactory.getLogger(InstrumentAudioEditorController.class);
  private final ProjectService projectService;
  private final UIStateService uiStateService;
  private final ObjectProperty<UUID> instrumentAudioId = new SimpleObjectProperty<>(null);
  private final StringProperty name = new SimpleStringProperty("");
  private final StringProperty event = new SimpleStringProperty("");
  private final FloatProperty volume = new SimpleFloatProperty(0.0f);
  private final StringProperty tones = new SimpleStringProperty("");
  private final FloatProperty tempo = new SimpleFloatProperty(0.0f);
  private final FloatProperty density = new SimpleFloatProperty(0.0f);
  private final FloatProperty transientSeconds = new SimpleFloatProperty(0.0f);
  private final FloatProperty totalBeats = new SimpleFloatProperty(0.0f);
  private final BooleanProperty dirty = new SimpleBooleanProperty(false);

  @FXML
  protected SplitPane container;

  @FXML
  protected VBox fieldsContainer;

  @FXML
  protected TextField fieldName;

  @FXML
  protected TextField fieldEvent;

  @FXML
  protected TextField fieldVolume;

  @FXML
  protected TextField fieldTones;

  @FXML
  protected TextField fieldTempo;

  @FXML
  protected TextField fieldDensity;

  @FXML
  protected TextField fieldTransientSeconds;

  @FXML
  protected TextField fieldTotalBeats;


  @FXML
  protected Button buttonSave;

  public InstrumentAudioEditorController(
    ProjectService projectService,
    UIStateService uiStateService
  ) {
    this.projectService = projectService;
    this.uiStateService = uiStateService;
  }

  @Override
  public void onStageReady() {
    var visible = projectService.isStateReadyProperty()
      .and(uiStateService.viewModeProperty().isEqualTo(ViewMode.Content))
      .and(uiStateService.contentModeProperty().isEqualTo(ContentMode.InstrumentAudioEditor));
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    fieldName.textProperty().bindBidirectional(name);
    fieldEvent.textProperty().bindBidirectional(event);
    // todo fieldVolume.textProperty().bindBidirectional(volume, new FloatStringConverter());
    fieldTones.textProperty().bindBidirectional(tones);
    // todo fieldTempo.textProperty().bindBidirectional(tempo, new FloatStringConverter());
    // todo fieldDensity.textProperty().bindBidirectional(density, new FloatStringConverter());
    // todo fieldTransientSeconds.textProperty().bindBidirectional(transientSeconds, new FloatStringConverter());
    // todo fieldTotalBeats.textProperty().bindBidirectional(totalBeats, new FloatStringConverter());

    name.addListener((o, ov, v) -> dirty.set(true));
    event.addListener((o, ov, v) -> dirty.set(true));
    volume.addListener((o, ov, v) -> dirty.set(true));
    tones.addListener((o, ov, v) -> dirty.set(true));
    tempo.addListener((o, ov, v) -> dirty.set(true));
    density.addListener((o, ov, v) -> dirty.set(true));
    transientSeconds.addListener((o, ov, v) -> dirty.set(true));
    totalBeats.addListener((o, ov, v) -> dirty.set(true));

    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ContentMode.InstrumentAudioEditor))
        update();
    });

    buttonSave.disableProperty().bind(dirty.not());
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  @FXML
  protected void handlePressSave() {
    var instrumentAudio = projectService.getContent().getInstrumentAudio(instrumentAudioId.get())
      .orElseThrow(() -> new RuntimeException("Could not find InstrumentAudio"));
    instrumentAudio.setName(name.get());
    instrumentAudio.setEvent(event.get());
    instrumentAudio.setVolume(volume.get());
    instrumentAudio.setTones(tones.get());
    instrumentAudio.setTempo(tempo.get());
    instrumentAudio.setDensity(density.get());
    instrumentAudio.setTransientSeconds(transientSeconds.get());
    instrumentAudio.setTotalBeats(totalBeats.get());
    if (projectService.updateInstrumentAudio(instrumentAudio)) dirty.set(false);
  }

  /**
   Update the Instrument Editor with the current Instrument.
   */
  private void update() {
    if (uiStateService.currentInstrumentAudioProperty().isNull().get())
      return;
    var instrumentAudio = projectService.getContent().getInstrumentAudio(uiStateService.currentInstrumentAudioProperty().get().getId())
      .orElseThrow(() -> new RuntimeException("Could not find InstrumentAudio"));
    LOG.info("Will edit InstrumentAudio \"{}\"", instrumentAudio.getName());
    instrumentAudioId.set(instrumentAudio.getId());
    name.set(instrumentAudio.getName());
    event.set(instrumentAudio.getEvent());
    volume.set(instrumentAudio.getVolume());
    tones.set(instrumentAudio.getTones());
    tempo.set(instrumentAudio.getTempo());
    density.set(instrumentAudio.getDensity());
    transientSeconds.set(instrumentAudio.getTransientSeconds());
    totalBeats.set(instrumentAudio.getTotalBeats());
    this.dirty.set(false);
  }
}
