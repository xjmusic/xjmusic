// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_AUDIO_LOADER_H
#define XJMUSIC_AUDIO_LOADER_H

namespace XJ {

  class AudioLoader{
  int MAX_INT_LENGTH_ARRAY_SIZE = 2147483647;
  int READ_BUFFER_BYTE_SIZE = 1024;

  public:
    /**
       * Load the specified audio file into memory
       *
       * @param audio instrument audio record
       * @return audio in memory
       * @throws IOException                   if file cannot be read
       * @throws UnsupportedAudioFileException if file is not supported
       */
      AudioInMemory load(InstrumentAudio audio) throws IOException, UnsupportedAudioFileException;

      /**
       * Load the specified audio file into memory
       *
       * @param audio  instrument audio record
       * @param path   to audio file
       * @param format of audio file
       * @return audio in memory
       * @throws IOException                   if file cannot be read
       * @throws UnsupportedAudioFileException if file is not supported
       */
      AudioInMemory load(InstrumentAudio audio, std::string path, AudioFormat format) throws IOException, UnsupportedAudioFileException;
  };

}// namespace XJ

#endif //XJMUSIC_AUDIO_LOADER_H