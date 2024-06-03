// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.instrument;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.types.Route;
import io.xj.gui.utils.ProjectUtils;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.util.StringUtils;
import io.xj.engine.audio.AudioInMemory;
import io.xj.engine.audio.AudioLoader;
import io.xj.engine.project.ProjectPathUtils;
import javafx.beans.binding.Bindings;
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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.converter.NumberStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.UUID;

import static io.xj.gui.services.UIStateService.OPEN_PSEUDO_CLASS;

@Service
public class InstrumentAudioEditorController extends BrowserController {
  static final Logger LOG = LoggerFactory.getLogger(InstrumentAudioEditorController.class);
  private static final int SCROLL_PANE_HBAR_HEIGHT = 15;
  private final ObjectProperty<UUID> instrumentAudioId = new SimpleObjectProperty<>(null);
  private final FloatProperty samplesPerPixel = new SimpleFloatProperty(100);
  private final FloatProperty zoomRatio = new SimpleFloatProperty(1.0f);
  private final IntegerProperty one = new SimpleIntegerProperty(1);
  private final IntegerProperty waveformViewportWidth = new SimpleIntegerProperty(1);
  private final IntegerProperty waveformWidth = new SimpleIntegerProperty(1);
  private final StringProperty name = new SimpleStringProperty("");
  private final StringProperty event = new SimpleStringProperty("");
  private final FloatProperty volume = new SimpleFloatProperty(0.0f);
  private final StringProperty tones = new SimpleStringProperty("");
  private final FloatProperty tempo = new SimpleFloatProperty(0.0f);
  private final FloatProperty intensity = new SimpleFloatProperty(0.0f);
  private final FloatProperty transientSeconds = new SimpleFloatProperty(0.0f);
  private final FloatProperty loopBeats = new SimpleFloatProperty(0.0f);
  private final BooleanProperty isSettingTransient = new SimpleBooleanProperty(false);
  private final BooleanProperty audioFileNotFound = new SimpleBooleanProperty(false);
  private final AudioLoader audioLoader;
  private final ObjectProperty<AudioInMemory> audioInMemory = new SimpleObjectProperty<>(null);
  private final Color waveformSampleColor;
  private final Color waveformTransientColor;
  private final Color waveformZeroColor;
  private final Color waveformGridColor;

  @Value("${instrumentAudioEditor.maxWidthPixels}")
  private int waveformMaxWidth;

  @Value("${instrumentAudioEditor.heightPixels}")
  private int waveformHeight;

  @Value("${instrumentAudioEditor.normalizeMaxValue}")
  private float waveformNormalizeMaxValue;

  @Value("${instrumentAudioEditor.minSamplesPerPixel}")
  private int waveformMinSamplesPerPixel;

  @Value("${instrumentAudioEditor.transientDashPixels}")
  private int waveformTransientDashPixels;

  @FXML
  SplitPane container;

  @FXML
  VBox fieldsContainer;

  @FXML
  TextField fieldName;

  @FXML
  TextField fieldEvent;

  @FXML
  TextField fieldVolume;

  @FXML
  TextField fieldTones;

  @FXML
  TextField fieldTempo;

  @FXML
  TextField fieldIntensity;

  @FXML
  TextField fieldTransientSeconds;

  @FXML
  Button setTransientButton;

  @FXML
  TextField fieldLoopBeats;

  @FXML
  ScrollPane waveformScrollPane;

  @FXML
  StackPane waveformContainer;

  @FXML
  ImageView waveform;

  @FXML
  Button buttonOpenAudioFolder;

  @FXML
  Button buttonOpenAudioFile;

  @FXML
  Label labelAudioFileName;

  @FXML
  Label labelAudioFileNotFound;

  @FXML
  Button buttonZoomOut;

  @FXML
  Button buttonZoomIn;

  public InstrumentAudioEditorController(
    @Value("classpath:/views/content/instrument/instrument-audio-editor.fxml") Resource fxml,
    @Value("${instrumentAudioEditor.gridColor}") String waveformGridColor,
    @Value("${instrumentAudioEditor.sampleColor}") String waveformSampleColor,
    @Value("${instrumentAudioEditor.transientColor}") String waveformTransientColor,
    @Value("${instrumentAudioEditor.zeroColor}") String waveformZeroColor,
    ApplicationContext ac,
    ThemeService themeService,
    ProjectService projectService,
    UIStateService uiStateService,
    AudioLoader audioLoader
  ) {
    super(fxml, ac, themeService, uiStateService, projectService);
    this.audioLoader = audioLoader;
    this.waveformGridColor = Color.valueOf(waveformGridColor);
    this.waveformSampleColor = Color.valueOf(waveformSampleColor);
    this.waveformTransientColor = Color.valueOf(waveformTransientColor);
    this.waveformZeroColor = Color.valueOf(waveformZeroColor);
  }

  @Override
  public void onStageReady() {
    var visible = Bindings.createBooleanBinding(
      () -> projectService.isStateReadyProperty().get()
        && uiStateService.navStateProperty().get() == Route.ContentInstrumentAudioEditor,
      projectService.isStateReadyProperty(),
      uiStateService.navStateProperty());
    container.visibleProperty().bind(visible);
    container.managedProperty().bind(visible);

    zoomRatio.addListener((o, ov, v) -> renderWaveform());
    buttonZoomIn.disableProperty().bind(waveformWidth.greaterThanOrEqualTo(waveformMaxWidth).or(samplesPerPixel.lessThan(waveformMinSamplesPerPixel)));
    buttonZoomOut.disableProperty().bind(zoomRatio.lessThanOrEqualTo(1));
    waveformViewportWidth.bind(container.getScene().widthProperty().multiply(one.subtract(container.getDividers().get(0).positionProperty())));
    waveform.fitHeightProperty().bind(container.heightProperty().subtract(SCROLL_PANE_HBAR_HEIGHT));

    fieldName.textProperty().bindBidirectional(name);
    fieldEvent.textProperty().bindBidirectional(event);
    fieldVolume.textProperty().bindBidirectional(volume, new NumberStringConverter());
    fieldTones.textProperty().bindBidirectional(tones);
    fieldTempo.textProperty().bindBidirectional(tempo, new NumberStringConverter());
    fieldIntensity.textProperty().bindBidirectional(intensity, new NumberStringConverter());
    fieldTransientSeconds.textProperty().bindBidirectional(transientSeconds, new NumberStringConverter());
    fieldLoopBeats.textProperty().bindBidirectional(loopBeats, new NumberStringConverter());
    labelAudioFileName.textProperty().bind(Bindings.createStringBinding(
      () -> audioFileNotFound.not().get() && audioInMemory.isNotNull().get() ? ProjectPathUtils.getFilename(audioInMemory.get().pathToAudioFile()) : "",
      audioInMemory, audioFileNotFound));

    fieldName.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        update("name", name.get());
      }
    });
    fieldEvent.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        update("event", event.get());
      }
    });
    fieldVolume.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        update("volume", volume.get());
      }
    });
    fieldTones.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        update("tones", tones.get());
      }
    });
    fieldIntensity.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        update("intensity", intensity.get());
      }
    });
    fieldTempo.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        update("tempo", tempo.get());
        renderWaveform();
      }
    });
    fieldLoopBeats.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        update("loopBeats", loopBeats.get());
        renderWaveform();
      }
    });
    fieldTransientSeconds.focusedProperty().addListener((o, ov, v) -> {
      if (!v) {
        update("transientSeconds", transientSeconds.get());
        renderWaveform();
      }
    });

    uiStateService.navStateProperty().addListener((o, ov, v) -> {
      if (Objects.equals(uiStateService.navStateProperty().get(), Route.ContentInstrumentAudioEditor))
        setup();
    });

    isSettingTransient.addListener((o, ov, active) -> {
      setTransientButton.pseudoClassStateChanged(OPEN_PSEUDO_CLASS, active);
      waveformContainer.setCursor(active ? javafx.scene.Cursor.CROSSHAIR : javafx.scene.Cursor.DEFAULT);
    });

    labelAudioFileNotFound.visibleProperty().bind(audioFileNotFound);
    labelAudioFileNotFound.managedProperty().bind(audioFileNotFound);
  }

  @Override
  public void onStageClose() {
    // FUTURE: on stage close
  }

  @FXML
  private void handlePressOpenAudioFolder() {
    var instrumentAudio = projectService.getContent().getInstrumentAudio(instrumentAudioId.get())
      .orElseThrow(() -> new RuntimeException("Could not find InstrumentAudio"));
    var instrument = projectService.getContent().getInstrument(instrumentAudio.getInstrumentId())
      .orElseThrow(() -> new RuntimeException("Could not find Instrument"));
    var audioFolder = projectService.getPathPrefixToInstrument(instrument);
    if (Objects.isNull(audioFolder)) return;
    ProjectUtils.openDesktopPath(audioFolder);
  }

  @FXML
  private void handlePressOpenAudioFile() {
    var instrumentAudio = projectService.getContent().getInstrumentAudio(instrumentAudioId.get())
      .orElseThrow(() -> new RuntimeException("Could not find InstrumentAudio"));
    var audioFile = projectService.getPathToInstrumentAudioWaveform(instrumentAudio);
    if (Objects.isNull(audioFile)) return;
    ProjectUtils.openDesktopPath(audioFile);
  }

  @FXML
  private void handlePressZoomOut() {
    zoomRatio.set(Math.max(1, zoomRatio.get() - 1));
  }

  @FXML
  private void handlePressZoomIn() {
    zoomRatio.set(zoomRatio.get() + 1);
  }

  @FXML
  private void handleClickedWaveformContainer(MouseEvent event) {
    if (audioInMemory.isNull().get()) return;
    if (isSettingTransient.not().get()) return;

    // The waveform is scaled based on the window height
    float scale = (float) (waveformHeight / waveform.fitHeightProperty().get());

    // Compute the X value that was clicked on the waveform, correcting for the scrolling of the pane that was clicked
    double hValue = waveformScrollPane.getHvalue();
    double contentWidth = waveformScrollPane.getContent().getLayoutBounds().getWidth();
    double viewportWidth = waveformScrollPane.getViewportBounds().getWidth();
    double maxScrollableWidth = Math.min(0, contentWidth - viewportWidth);
    double scrolledPixelsRight = hValue * maxScrollableWidth;
    double x = event.getX() + scrolledPixelsRight;

    // Set the transient
    transientSeconds.set((float) (scale * x * samplesPerPixel.get() / audioInMemory.get().format().getSampleRate()));
    update("transientSeconds", transientSeconds.get());
    renderWaveform();

    // Done with transient setting mode
    isSettingTransient.set(false);

    // No further processing of the event
    event.consume();
  }

  @FXML
  void handleSetTransient() {
    isSettingTransient.set(!isSettingTransient.get());
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
    loopBeats.set(instrumentAudio.getLoopBeats());
    zoomRatio.set(1.0f);
    audioFileNotFound.set(false);
    renderWaveform();
  }

  /**
   Update an attribute of the current instrumentAudio record with the given value

   @param attribute of instrumentAudio
   @param value     to set
   */
  private void update(String attribute, Object value) {
    if (Objects.nonNull(instrumentAudioId.get())) {
      try {
        projectService.update(InstrumentAudio.class, instrumentAudioId.get(), attribute, value);
      } catch (Exception e) {
        LOG.error("Could not update InstrumentAudio \"{}\"! {}\n{}", attribute, e, StringUtils.formatStackTrace(e));
      }
    }
  }

  /**
   Read waveform file into buffer of floating point samples, create a Polyline for each channel, and add it to the waveformContainer
   */
  private void renderWaveform() {
    try {
      loadAudioWaveform();
      if (audioInMemory.isNull().get()) return;

      // Calculate the total number of pixels needed to represent the waveform
      int totalSamples = audioInMemory.get().data().length;
      waveformWidth.set(Math.min(waveformMaxWidth, (int) (zoomRatio.get() * waveformViewportWidth.get())));
      samplesPerPixel.set((float) totalSamples / waveformWidth.get());
      int channelRadius = waveformHeight / audioInMemory.get().format().getChannels() / 2;
      int channelDiameter = channelRadius * 2;
      float displayVolumeRatio = computeDisplayVolumeRatio(audioInMemory.get().data());
      float secondsPerBeat = 60 / tempo.get();
      int beatsBeforeTransient = ((int) (transientSeconds.get() / secondsPerBeat));
      int beatsAfterTransient = ((int) ((audioInMemory.get().data().length / audioInMemory.get().format().getSampleRate()) / secondsPerBeat));

      // Create a new image to hold the waveform
      WritableImage image = new WritableImage(waveformWidth.get(), waveformHeight);
      PixelWriter pixelWriter = image.getPixelWriter();

      // for pixel calculations
      int i;
      int x;
      int y;

      // Draw the zero line
      for (x = 0; x < waveformWidth.get(); x++) {
        for (int channel = 0; channel < audioInMemory.get().data()[0].length; channel++) {
          pixelWriter.setColor(x, channelDiameter * channel + channelRadius, waveformZeroColor);
        }
      }

      // Draw the beat grid lines
      for (i = -beatsBeforeTransient; i < beatsAfterTransient; i++) {
        if (i == 0) continue; // don't draw at the transient
        x = (int) Math.max(0, Math.min(waveformWidth.get() - 1, ((transientSeconds.get() + i * secondsPerBeat) * audioInMemory.get().format().getSampleRate() / samplesPerPixel.get())));
        for (y = 0; y < waveformHeight; y++) {
          pixelWriter.setColor(x, y, waveformGridColor);
        }
      }

      // Draw the transient dashed line
      x = (int) Math.max(0, Math.min(waveformWidth.get() - 1, (transientSeconds.get() * audioInMemory.get().format().getSampleRate() / samplesPerPixel.get())));
      for (y = 0; y < waveformHeight; y++) {
        if ((y / waveformTransientDashPixels) % 2 == 0) {
          pixelWriter.setColor(x, y, waveformTransientColor);
        }
      }

      // Draw each sample
      for (i = 0; i < audioInMemory.get().data().length; i++) {
        x = (int) (i / samplesPerPixel.get());
        for (int channel = 0; channel < audioInMemory.get().data()[i].length; channel++) {
          y = channelDiameter * channel + channelRadius - (int) (audioInMemory.get().data()[i][channel] * channelRadius * displayVolumeRatio);
          if (y >= 0 && y <= waveformHeight) pixelWriter.setColor(x, y, waveformSampleColor);
        }
      }

      // Set the image to the waveform
      waveform.setImage(image);
      waveform.setLayoutX(0);

    } catch (Exception e) {
      LOG.error("Could not render audioInMemory file! {}\n{}", e, StringUtils.formatStackTrace(e));
    }
  }

  /**
   Load the audio waveform into memory if it is not already loaded, or return cached audio in memory
   */
  private void loadAudioWaveform() {
    if (uiStateService.currentInstrumentAudioProperty().isNull().get()) return;
    var audio = projectService.getContent().getInstrumentAudio(uiStateService.currentInstrumentAudioProperty().get().getId())
      .orElseThrow(() -> new RuntimeException("Could not find InstrumentAudio"));
    if (StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
      LOG.warn("No waveform file found for InstrumentAudio \"{}\"", audio.getName());
      projectService.showWarningAlert(
        "No waveform file found",
        "No waveform file found for this InstrumentAudio.",
        "Please delete this audio and re-create with a valid waveform."
      );
      return;
    }
    if (audioInMemory.isNull().get() || audioInMemory.get().isDifferent(audio)) try {
      audioInMemory.set(audioLoader.load(audio));
      LOG.info("Loaded audio file \"{}\" with {} channels and {} frames", audioInMemory.get().format(), audioInMemory.get().format().getChannels(), audioInMemory.get().data().length);
    } catch (FileNotFoundException e) {
      waveform.setImage(null);
      audioInMemory.set(null);
      audioFileNotFound.set(true);
      projectService.showWarningAlert(
        "Could not find audio file",
        "Could not find audio file",
        "Could not find audio the audio file on disk: " + projectService.getPathToInstrumentAudioWaveform(audio) + "\n\nPlease re-import the audio file."
      );

    } catch (Exception e) {
      waveform.setImage(null);
      audioInMemory.set(null);
      LOG.error("Could not load audio file! {}\n{}", e, StringUtils.formatStackTrace(e));
      projectService.showWarningAlert(
        "Could not load audio file",
        "Could not load audio file.",
        "Please try again or contact support."
      );
    }
  }

  /**
   Compute the display volume ratio by iterating through all samples and channels and finding the max value,
   then determining the ratio that will max that max value equal 0.9.

   @param audio the audio samples
   @return the display volume ratio
   */
  private float computeDisplayVolumeRatio(float[][] audio) {
    float max = 0;
    for (float[] samples : audio) {
      for (float sample : samples) {
        max = Math.max(max, Math.abs(sample));
      }
    }
    return waveformNormalizeMaxValue / max;
  }
}
