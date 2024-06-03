package io.xj.engine.audio;

import io.xj.hub.pojos.InstrumentAudio;

import javax.sound.sampled.AudioFormat;
import java.util.UUID;

/**
 * Represents an audio file loaded into memory.@param id
 *
 * @param format          the audio format of the audio file
 * @param pathToAudioFile the path to the audio file
 * @param data            2D array of floats representing the audio file-- the first channel is the sample index, the second channel is the channel index
 */
public record AudioInMemory(InstrumentAudio audio, AudioFormat format, String pathToAudioFile, float[][] data) {
  public UUID getId() {
    return audio.getId();
  }

  public String getWaveformKey() {
    return audio.getWaveformKey();
  }

  public boolean isDifferent(InstrumentAudio other) {
    return !audio.getId().equals(other.getId()) || !audio.getWaveformKey().equals(other.getWaveformKey());
  }
}
