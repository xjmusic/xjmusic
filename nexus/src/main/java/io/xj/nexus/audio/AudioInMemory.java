package io.xj.nexus.audio;

import javax.sound.sampled.AudioFormat;

/**
 Represents an audio file loaded into memory.

 @param audio           2D array of floats representing the audio file-- the first channel is the sample index, the second channel is the channel index
 @param format          the audio format of the audio file
 @param pathToAudioFile the path to the audio file
 */
public record AudioInMemory(float[][] audio, AudioFormat format, String pathToAudioFile) {
}
