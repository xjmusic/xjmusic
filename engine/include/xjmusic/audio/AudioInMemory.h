// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_AUDIO_IN_MEMORY_H
#define XJMUSIC_AUDIO_IN_MEMORY_H

#include "xjmusic/content/InstrumentAudio.h"
#include "AudioFormat.h"

namespace XJ {

/**
 * Represents an audio file loaded into memory.@param id
 *
 * @param format          the audio format of the audio file
 * @param pathToAudioFile the path to the audio file
 * @param data            2D array of floats representing the audio file-- the first channel is the sample index, the second channel is the channel index
 */
  class AudioInMemory {
    InstrumentAudio audio;
    AudioFormat format;
    std::string pathToAudioFile;
    std::vector<std::vector<float>> data;

  public:
    UUID getId();
    std::string getWaveformKey();
    bool isDifferent(InstrumentAudio other);
  };

}// namespace XJ

#endif //XJMUSIC_AUDIO_IN_MEMORY_H