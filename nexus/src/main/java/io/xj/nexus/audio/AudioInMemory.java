package io.xj.nexus.audio;

import javax.sound.sampled.AudioFormat;
import java.util.UUID;

/**
 Represents an audio file loaded into memory.@param id

 @param format          the audio format of the audio file
 @param pathToAudioFile the path to the audio file
 @param data            2D array of floats representing the audio file-- the first channel is the sample index, the second channel is the channel index */
public record AudioInMemory(UUID id, AudioFormat format, String pathToAudioFile, float[][] data) {
}
