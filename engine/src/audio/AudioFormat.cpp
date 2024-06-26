// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/audio/AudioFormat.h"

using namespace XJ;

AudioFormat::AudioFormat(AudioFormat::Encoding encoding, float sampleRate, int sampleSizeInBits, int channels,
                         int frameSize, float frameRate, bool bigEndian) {
  this->encoding = encoding;
  this->sampleRate = sampleRate;
  this->sampleSizeInBits = sampleSizeInBits;
  this->channels = channels;
  this->frameSize = frameSize;
  this->frameRate = frameRate;
  this->bigEndian = bigEndian;
}

AudioFormat::AudioFormat(float sampleRate, int sampleSizeInBits, int channels, bool isSigned, bool bigEndian) : AudioFormat(
    (isSigned == true ? Encoding::PCM_SIGNED : Encoding::PCM_UNSIGNED),
    sampleRate,
    sampleSizeInBits,
    channels,
    ((sampleSizeInBits + 7) / 8) * channels,
    sampleRate,
    bigEndian
) {}

AudioFormat::Encoding AudioFormat::getEncoding() const {
  return encoding;
}

float AudioFormat::getSampleRate() const {
  return sampleRate;
}

int AudioFormat::getSampleSizeInBits() const {
  return sampleSizeInBits;
}

int AudioFormat::getChannels() const {
  return channels;
}

int AudioFormat::getFrameSize() const {
  return frameSize;
}

float AudioFormat::getFrameRate() const {
  return frameRate;
}

bool AudioFormat::isBigEndian() const {
  return bigEndian;
}

bool AudioFormat::matches(AudioFormat format) const {
  if (format.getEncoding() == getEncoding()
      && format.getChannels() == getChannels()
      && format.getSampleRate() == getSampleRate()
      && format.getSampleSizeInBits() == getSampleSizeInBits()
      && format.getFrameRate() == getFrameRate()
      && format.getFrameSize() == getFrameSize()
      && (getSampleSizeInBits() <= 8 || format.isBigEndian() == isBigEndian())) {
    return true;
  }
  return false;
}

std::string AudioFormat::toString() {
  std::string sampleRateStr = std::to_string(getSampleRate()) + " Hz";

  std::string sampleSizeStr = std::to_string(getSampleSizeInBits()) + " bit";

  std::string channelsStr;
  switch (getChannels()) {
    case 1:
      channelsStr = "mono";
      break;
    case 2:
      channelsStr = "stereo";
      break;
    default:
      channelsStr = std::to_string(getChannels()) + " channels";
  };

  std::string frameSizeStr = std::to_string(getFrameSize()) + " bytes/frame";

  std::string frameRateStr;
  if (abs(getSampleRate() - getFrameRate()) > 0.00001) {
    frameRateStr = ", " + std::to_string(getFrameRate()) + " frames/second";
  }

  std::string bigEndianStr;
  if ((getEncoding() == Encoding::PCM_SIGNED
       || getEncoding() == Encoding::PCM_UNSIGNED)
      && getSampleSizeInBits() > 8)
    bigEndianStr = isBigEndian() ? ", big-endian" : ", little-endian";

  std::string encodingStr;
  switch (encoding) {
    case Encoding::PCM_SIGNED:
      encodingStr = "PCM SIGNED";
      break;
    case Encoding::PCM_UNSIGNED:
      encodingStr = "PCM UNSIGNED";
      break;
    case Encoding::PCM_FLOAT:
      encodingStr = "PCM FLOAT";
      break;
    case Encoding::ULAW:
      encodingStr = "ULAW";
      break;
    case Encoding::ALAW:
      encodingStr = "ALAW";
      break;
    default:
      encodingStr = "Unknown encoding";
      break;
  }

  return encodingStr + " " + sampleRateStr + ", " + sampleSizeStr + ", " + channelsStr + ", " +
         frameSizeStr + frameRateStr + bigEndianStr;
}
