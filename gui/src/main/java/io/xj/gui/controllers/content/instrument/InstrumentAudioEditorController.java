// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.gui.controllers.content.instrument;

import io.xj.gui.controllers.BrowserController;
import io.xj.gui.modes.ContentMode;
import io.xj.gui.modes.ViewMode;
import io.xj.gui.services.ProjectService;
import io.xj.gui.services.ThemeService;
import io.xj.gui.services.UIStateService;
import io.xj.gui.utils.ProjectUtils;
import io.xj.hub.util.StringUtils;
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
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
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

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class InstrumentAudioEditorController extends BrowserController {
    static final Logger LOG = LoggerFactory.getLogger(InstrumentAudioEditorController.class);
    private static final int SCROLL_PANE_HBAR_HEIGHT = 15;
    private final ObjectProperty<UUID> instrumentAudioId = new SimpleObjectProperty<>(null);
    private final IntegerProperty samplesPerPixel = new SimpleIntegerProperty(100);
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
    private final FloatProperty totalBeats = new SimpleFloatProperty(0.0f);
    private final BooleanProperty dirty = new SimpleBooleanProperty(false);
    private final int waveformMaxWidth;
    private final int waveformHeight;
    private final float displayNormalizeMaxValue;
    private final AudioLoader audioLoader;
    private final ObjectProperty<AudioInMemory> audioInMemory = new SimpleObjectProperty<>(null);

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
    protected ScrollPane waveformScrollPane;

    @FXML
    protected StackPane waveformContainer;

    @FXML
    protected ImageView waveform;

    @FXML
    protected Button buttonOpenAudioFile;

    @FXML
    protected Button buttonZoomOut;

    @FXML
    protected Button buttonZoomIn;

    public InstrumentAudioEditorController(
            @Value("${gui.instrument.audio.waveform.display.normalize.max.value}") float displayNormalizeMaxValue,
            @Value("${gui.instrument.audio.waveform.height.pixels}") int waveformHeight,
            @Value("${gui.instrument.audio.waveform.max.width.pixels}") int waveformMaxWidth,
            @Value("classpath:/views/content/instrument/instrument-audio-editor.fxml") Resource fxml,
            ApplicationContext ac,
            ThemeService themeService,
            ProjectService projectService,
            UIStateService uiStateService,
            AudioLoader audioLoader
    ) {
        super(fxml, ac, themeService, uiStateService, projectService);
        this.audioLoader = audioLoader;
        this.displayNormalizeMaxValue = displayNormalizeMaxValue;
        this.waveformHeight = waveformHeight;
        this.waveformMaxWidth = waveformMaxWidth;
    }

    @Override
    public void onStageReady() {
        var visible = projectService.isStateReadyProperty()
                .and(uiStateService.viewModeProperty().isEqualTo(ViewMode.Content))
                .and(uiStateService.contentModeProperty().isEqualTo(ContentMode.InstrumentAudioEditor));
        container.visibleProperty().bind(visible);
        container.managedProperty().bind(visible);

        waveformViewportWidth.bind(container.getScene().widthProperty().multiply(one.subtract(container.getDividers().get(0).positionProperty())));
        waveform.fitHeightProperty().bind(container.heightProperty().subtract(SCROLL_PANE_HBAR_HEIGHT));
        zoomRatio.addListener((o, ov, v) -> renderWaveform());
        buttonZoomIn.disableProperty().bind(waveformWidth.greaterThanOrEqualTo(waveformMaxWidth));
        buttonZoomOut.disableProperty().bind(zoomRatio.lessThanOrEqualTo(1));

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
        zoomRatio.set(1.0f);
        renderWaveform();
    }

    /**
     Read waveform file into buffer of floating point samples, create a Polyline for each channel, and add it to the waveformContainer
     */
    private void renderWaveform() {
        try {
            Optional<AudioInMemory> audio = loadAudioWaveform();
            if (audio.isEmpty()) return;

            // Calculate the total number of pixels needed to represent the waveform
            int totalSamples = audio.get().audio().length;
            waveformWidth.set(Math.min(waveformMaxWidth, (int) (zoomRatio.get() * waveformViewportWidth.get())));
            samplesPerPixel.set(totalSamples / waveformWidth.get());
            int channelRadius = waveformHeight / audio.get().format().getChannels() / 2;
            int channelDiameter = channelRadius * 2;
            float displayVolumeRatio = computeDisplayVolumeRatio(audio.get().audio());

            // Create a new image to hold the waveform
            WritableImage image = new WritableImage(waveformWidth.get(), waveformHeight);
            PixelWriter pixelWriter = image.getPixelWriter();
            Color color = uiStateService.getWaveformColor();

            // Calculate the total number of pixels needed to represent the waveform
            int startSampleIndex;
            int endSampleIndex;

            // For each pixel, get the max value of the samples in that range and add it to the polyline
            for (int x = 0; x < waveformWidth.get(); x++) {
                startSampleIndex = x * samplesPerPixel.get();
                endSampleIndex = Math.min(startSampleIndex + samplesPerPixel.get(), totalSamples);
                writeSamplesToImage(audio.get().audio(), pixelWriter, color, displayVolumeRatio, channelRadius, channelDiameter, startSampleIndex, endSampleIndex, x);
            }

            // Set the image to the waveform
            waveform.setImage(image);
            waveform.setLayoutX(0);

        } catch (Exception e) {
            LOG.error("Could not render audio file!\n{}", StringUtils.formatStackTrace(e), e);
        }
    }

    /**
     Load the audio waveform into memory if it is not already loaded, or return cached audio in memory

     @return the audio in memory
     */
    private Optional<AudioInMemory> loadAudioWaveform() {
        var audio = projectService.getContent().getInstrumentAudio(uiStateService.currentInstrumentAudioProperty().get().getId())
                .orElseThrow(() -> new RuntimeException("Could not find InstrumentAudio"));
        if (StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
            LOG.warn("No waveform file found for InstrumentAudio \"{}\"", audio.getName());
            projectService.showWarningAlert(
                    "No waveform file found",
                    "No waveform file found for this InstrumentAudio.",
                    "Please delete this audio and re-create with a valid waveform."
            );
            return Optional.empty();
        }
        if (audioInMemory.isNull().get() || !audioInMemory.get().id().equals(audio.getId())) try {
            audioInMemory.set(audioLoader.load(audio));
            LOG.info("Loaded audio file \"{}\" with {} channels and {} frames", audioInMemory.get().format(), audioInMemory.get().format().getChannels(), audioInMemory.get().audio().length);
        } catch (Exception e) {
            LOG.error("Could not load audio file!\n{}", StringUtils.formatStackTrace(e), e);
            projectService.showWarningAlert(
                    "Could not load audio file",
                    "Could not load audio file.",
                    "Please try again or contact support."
            );
            return Optional.empty();
        }
        return Optional.of(audioInMemory.get());
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
        return displayNormalizeMaxValue / max;
    }

    /**
     Write samples to waveform image at a single X coordinate

     @param data             the audio samples
     @param pixelWriter      to write image
     @param color            to write
     @param ratio            the display volume ratio
     @param channelRadius    the radius of one channel
     @param channelDiameter  the diameter of one channel
     @param startSampleIndex the start sample index
     @param endSampleIndex   the end sample index
     @param x                the x position of the rectangle
     */
    private void writeSamplesToImage(float[][] data, PixelWriter pixelWriter, Color color, float ratio, int channelRadius, int channelDiameter, int startSampleIndex, int endSampleIndex, int x) {
        // keep track of old y for each channel and don't redraw if it hasn't changed
        int[] oy = new int[data[0].length];
        int y;
        for (int sampleIndex = startSampleIndex; sampleIndex < endSampleIndex; sampleIndex++) {
            for (int channel = 0; channel < data[sampleIndex].length; channel++) {
                y = channelDiameter * channel + channelRadius + (int) (data[sampleIndex][channel] * channelRadius * ratio);
                if (y != oy[channel]) {
                    oy[channel] = y;
                    if (y >= 0 && y <= waveformHeight) pixelWriter.setColor(x, y, color);
                }
            }
        }
    }
}
