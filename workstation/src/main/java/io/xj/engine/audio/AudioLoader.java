package io.xj.engine.audio;

import io.xj.model.pojos.InstrumentAudio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface AudioLoader {
  /**
   Load the specified audio file into memory

   @param audio instrument audio record
   @param path  to audio file
   @return audio in memory
   @throws IOException                   if file cannot be read
   @throws UnsupportedAudioFileException if file is not supported
   */
  AudioInMemory load(InstrumentAudio audio, String path) throws IOException, UnsupportedAudioFileException;

  /**
   Load the specified audio file into memory

   @param audio  instrument audio record
   @param path   to audio file
   @param format of audio file
   @return audio in memory
   @throws IOException                   if file cannot be read
   @throws UnsupportedAudioFileException if file is not supported
   */
  AudioInMemory load(InstrumentAudio audio, String path, AudioFormat format) throws IOException, UnsupportedAudioFileException;
}
