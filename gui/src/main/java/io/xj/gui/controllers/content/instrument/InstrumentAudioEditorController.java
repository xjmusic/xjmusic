// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.instrument;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.nexus.audio.AudioInMemory;
import io.xj.nexus.audio.AudioLoader;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Polyline;
import javafx.util.converter.NumberStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
public class InstrumentAudioEditorController extends BrowserController {
  static final Logger LOG = LoggerFactory.getLogger(InstrumentAudioEditorController.class);
  private final ObjectProperty<UUID> instrumentAudioId = new SimpleObjectProperty<>(null);
  private final IntegerProperty samplesPerPixel = new SimpleIntegerProperty(100);
  private final float WAVEFORM_REFERENCE_HEIGHT = 1000.0f;
  private final StringProperty name = new SimpleStringProperty("");
  private final StringProperty event = new SimpleStringProperty("");
  private final FloatProperty volume = new SimpleFloatProperty(0.0f);
  private final StringProperty tones = new SimpleStringProperty("");
  private final FloatProperty tempo = new SimpleFloatProperty(0.0f);
  private final FloatProperty intensity = new SimpleFloatProperty(0.0f);
  private final FloatProperty transientSeconds = new SimpleFloatProperty(0.0f);
  private final FloatProperty totalBeats = new SimpleFloatProperty(0.0f);
  private final BooleanProperty dirty = new SimpleBooleanProperty(false);
  private final AudioLoader audioLoader;

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
  protected TextField fieldIntensity;

  @FXML
  protected TextField fieldTransientSeconds;

  @FXML
  protected TextField fieldTotalBeats;

  @FXML
  protected Button buttonSave;

  @FXML
  protected AnchorPane waveformContainer;

  public InstrumentAudioEditorController(
    @Value("classpath:/views/content/instrument/instrument-audio-editor.fxml") Resource fxml,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService,
    AudioLoader audioLoader
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.audioLoader = audioLoader;
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
    fieldVolume.textProperty().bindBidirectional(volume, new NumberStringConverter());
    fieldTones.textProperty().bindBidirectional(tones);
    fieldTempo.textProperty().bindBidirectional(tempo, new NumberStringConverter());
    fieldIntensity.textProperty().bindBidirectional(intensity, new NumberStringConverter());
    fieldTransientSeconds.textProperty().bindBidirectional(transientSeconds, new NumberStringConverter());
    fieldTotalBeats.textProperty().bindBidirectional(totalBeats, new NumberStringConverter());

    name.addListener((o, ov, v) -> dirty.set(true));
    event.addListener((o, ov, v) -> dirty.set(true));
    volume.addListener((o, ov, v) -> dirty.set(true));
    tones.addListener((o, ov, v) -> dirty.set(true));
    tempo.addListener((o, ov, v) -> dirty.set(true));
    intensity.addListener((o, ov, v) -> dirty.set(true));
    transientSeconds.addListener((o, ov, v) -> dirty.set(true));
    totalBeats.addListener((o, ov, v) -> dirty.set(true));

    uiStateService.contentModeProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.contentModeProperty().get(), ContentMode.InstrumentAudioEditor))
        setup();
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
    instrumentAudio.setIntensity(intensity.get());
    instrumentAudio.setTransientSeconds(transientSeconds.get());
    instrumentAudio.setTotalBeats(totalBeats.get());
    if (projectService.updateInstrumentAudio(instrumentAudio)) dirty.set(false);
  }

  /**
   Update the Instrument Editor with the current Instrument.
   */
  private void setup() {
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
    intensity.set(instrumentAudio.getIntensity());
    transientSeconds.set(instrumentAudio.getTransientSeconds());
    totalBeats.set(instrumentAudio.getTotalBeats());
    this.dirty.set(false);

    // Read waveform file into buffer of floating point samples, create a Polyline for each channel, and add it to the waveformContainer
    try {
      var audioInMemory = audioLoader.load(instrumentAudio);
      LOG.info("Loaded audio file \"{}\" with {} channels and {} frames", audioInMemory.format(), audioInMemory.format().getChannels(), audioInMemory.audio().length);

      Group waveform = new Group();
      waveform.getStyleClass().add("waveform");
      int totalSamples = audioInMemory.audio().length;

      // Calculate the total number of pixels needed to represent the waveform
      int totalPixels = totalSamples / samplesPerPixel.get();
      int startSampleIndex;
      int endSampleIndex;

      // For each pixel, get the max value of the samples in that range and add it to the polyline
      for (int pixel = 0; pixel < totalPixels; pixel++) {
        startSampleIndex = pixel * samplesPerPixel.get();
        endSampleIndex = Math.min(startSampleIndex + samplesPerPixel.get(), totalSamples);
        waveform.getChildren().add(buildWaveformPolyline(startSampleIndex, endSampleIndex, audioInMemory, pixel));
      }

      waveformContainer.getChildren().clear();
      waveformContainer.getChildren().add(waveform);
      waveform.translateYProperty().bind(waveformContainer.heightProperty().divide(2));
      waveform.scaleYProperty().bind(waveformContainer.heightProperty().divide(2 * WAVEFORM_REFERENCE_HEIGHT));

    } catch (Exception e) {
      LOG.error("Could not load audio file", e);
    }
  }

  /**
   Get the max value from a range of samples in an audio file.

   @param startSampleIndex the start sample index
   @param endSampleIndex   the end sample index
   @param audioInMemory    the audio file
   @param x                the x position of the rectangle
   @return the max value
   */
  private Polyline buildWaveformPolyline(int startSampleIndex, int endSampleIndex, AudioInMemory audioInMemory, int x) {
    float max = -1;
    float min = 1;
    for (int sampleIndex = startSampleIndex; sampleIndex < endSampleIndex; sampleIndex++) {
      for (int channel = 0; channel < audioInMemory.audio()[sampleIndex].length; channel++) {
        max = Math.max(max, audioInMemory.audio()[sampleIndex][channel]);
        min = Math.min(min, audioInMemory.audio()[sampleIndex][channel]);
      }
    }
    var line = new Polyline(x, (int) (min * WAVEFORM_REFERENCE_HEIGHT), x, (int) (max * WAVEFORM_REFERENCE_HEIGHT));
    line.setStroke(uiStateService.getWaveformColor());
    return line;
  }
}
