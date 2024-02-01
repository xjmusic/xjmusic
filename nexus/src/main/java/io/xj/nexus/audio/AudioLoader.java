package io.xj.nexus.audio;

import io.xj.hub.tables.pojos.InstrumentAudio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface AudioLoader {
  /**
   Load the specified audio file into memory

   @param audio instrument audio record
   @return audio in memory
   @throws IOException                   if file cannot be read
   @throws UnsupportedAudioFileException if file is not supported
   */
  AudioInMemory load(InstrumentAudio audio) throws IOException, UnsupportedAudioFileException;

  /**
   Load the specified audio file into memory

   @param path   to audio file
   @param format of audio file
   @return audio in memory
   @throws IOException                   if file cannot be read
   @throws UnsupportedAudioFileException if file is not supported
   */
  AudioInMemory load(String path, AudioFormat format) throws IOException, UnsupportedAudioFileException;
}
