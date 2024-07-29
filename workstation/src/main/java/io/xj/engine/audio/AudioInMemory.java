package io.xj.engine.audio;

import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.util.ValueUtils;

import javax.sound.sampled.AudioFormat;
import java.util.UUID;

/**
 Represents an audio file loaded into memory.@param id

 @param format          the audio format of the audio file
 @param pathToAudioFile the path to the audio file
 @param data            2D array of floats representing the audio file-- the first channel is the sample index, the second channel is the channel index */
public record AudioInMemory(
  InstrumentAudio audio,
  AudioFormat format,
  String pathToAudioFile,
  float[][] data
) {

  /**
   Get the ID of the audio file

   @return the ID of the audio file
   */
  public UUID getId() {
    return audio.getId();
  }

  /**
   Get the filename of the audio file

   @return the filename of the audio file
   */
  public String getWaveformKey() {
    return audio.getWaveformKey();
  }

  /**
   Get the length in seconds of the audio file

   @return the length in seconds of the audio file
   */
  public Float lengthSeconds() {
    return ValueUtils.limitDecimalPrecision6(data.length / format.getSampleRate());
  }

  /**
   Check if the audio file is different from another audio file

   @param other the other audio file
   @return true if the audio file is different from the other audio file
   */
  public boolean isDifferent(InstrumentAudio other) {
    return !audio.getId().equals(other.getId()) || !audio.getWaveformKey().equals(other.getWaveformKey());
  }
}
