package io.xj.gui.services.impl;

import io.xj.gui.services.CompilationService;
import io.xj.gui.types.AudioFileContainer;
import io.xj.gui.types.CompilationState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.prefs.Preferences;

@Service
public class CompilationServiceImpl implements CompilationService {
  private static final Logger LOG = LoggerFactory.getLogger(CompilationServiceImpl.class);
  private final Preferences prefs = Preferences.userNodeForPackage(CompilationServiceImpl.class);
  private final int defaultOutputChannels;
  private final int defaultOutputFrameRate;
  private final AudioFileContainer defaultOutputContainer;
  private final StringProperty outputFrameRate = new SimpleStringProperty();
  private final StringProperty outputChannels = new SimpleStringProperty();
  private final ObjectProperty<AudioFileContainer> outputContainer = new SimpleObjectProperty<>();
  private final ObjectProperty<CompilationState> state = new SimpleObjectProperty<>(CompilationState.Standby);

  public CompilationServiceImpl(
    @Value("${compilation.defaultOutputChannels}") int defaultOutputChannels,
    @Value("${compilation.defaultOutputFrameRate}") int defaultOutputFrameRate,
    @Value("${compilation.defaultOutputContainer}") String defaultOutputContainer
  ) {
    this.defaultOutputChannels = defaultOutputChannels;
    this.defaultOutputFrameRate = defaultOutputFrameRate;
    this.defaultOutputContainer = AudioFileContainer.valueOf(defaultOutputContainer.toUpperCase(Locale.ROOT));

    attachPreferenceListeners();
    setAllFromPreferencesOrDefaults();
  }

  @Override
  public void compile() {

  }

  @Override
  public void resetSettingsToDefaults() {
    outputChannels.set(String.valueOf(defaultOutputChannels));
    outputFrameRate.set(String.valueOf(defaultOutputFrameRate));
  }

  @Override
  public StringProperty outputChannelsProperty() {
    return outputChannels;
  }

  @Override
  public StringProperty outputFrameRateProperty() {
    return outputFrameRate;
  }

  @Override
  public Property<AudioFileContainer> outputContainerProperty() {
    return outputContainer;
  }

  @Override
  public ObjectProperty<CompilationState> stateProperty() {
    return state;
  }

  /**
   Attach preference listeners.
   */
  private void attachPreferenceListeners() {
    outputChannels.addListener((o, ov, value) -> prefs.put("outputChannels", value));
    outputFrameRate.addListener((o, ov, value) -> prefs.put("outputFrameRate", value));
    outputContainer.addListener((o, ov, value) -> prefs.put("outputContainer", value.toString()));
  }

  /**
   Set all properties from preferences, else defaults.
   */
  private void setAllFromPreferencesOrDefaults() {
    outputChannels.set(prefs.get("outputChannels", Integer.toString(defaultOutputChannels)));
    outputFrameRate.set(prefs.get("outputFrameRate", Double.toString(defaultOutputFrameRate)));

    try {
      outputContainer.set(AudioFileContainer.valueOf(prefs.get("outputContainer", defaultOutputContainer.toString()).toUpperCase(Locale.ROOT)));
    } catch (Exception e) {
      LOG.error("Failed to set control mode from preferences", e);
      outputContainer.set(defaultOutputContainer);
    }
  }
}
