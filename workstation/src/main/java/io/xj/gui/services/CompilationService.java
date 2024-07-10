package io.xj.gui.services;

import io.xj.gui.types.AudioFileContainer;
import io.xj.gui.types.CompilationState;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

/**
 Workstation has a "Compile" button to prepare assets for the Unreal Plugin
 https://github.com/xjmusic/xjmusic/issues/421
 */
public interface CompilationService {

  /**
   - Press the Compile button to compile the current XJ project for use in another tool, i.e. the Unreal Engine via the XJ plugin.
     - Create a `build` folder inside the current XJ project folder, if it doesn't already exist.
     - Resample all referenced audio to unique file names and save these names as the `waveformKey` in out output project content (this is how the current template export process works)
     - Export the project content as a single file named after the project the .json extension, i.e. my-project-name.json
     - Delete dereferenced audio files in the build folder
     - Compile button has state-based appearance
   */
  void compile();

  void resetSettingsToDefaults();

  StringProperty outputChannelsProperty();

  StringProperty outputFrameRateProperty();

  Property<AudioFileContainer> outputContainerProperty();

  ObjectProperty<CompilationState> stateProperty();
}
