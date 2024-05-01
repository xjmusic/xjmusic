// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.audio;

import io.xj.hub.pojos.InstrumentAudio;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.mixer.FFmpegUtils;
import io.xj.nexus.project.ProjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AudioCacheImpl implements AudioCache {
  private final static Logger LOG = LoggerFactory.getLogger(AudioCacheImpl.class);
  private final ProjectManager projectManager;
  private final AudioLoader audioLoader;
  private final Map<String, AudioInMemory> cache;
  private int targetFrameRate;
  private int targetSampleBits;
  private int targetChannels;

  public AudioCacheImpl(
    ProjectManager projectManager,
    AudioLoader audioLoader
  ) {
    this.projectManager = projectManager;
    this.audioLoader = audioLoader;

    cache = new ConcurrentHashMap<>();
  }

  @Override
  public AudioInMemory load(InstrumentAudio audio) throws AudioCacheException, IOException, NexusException {
    if (!cache.containsKey(audio.getId().toString())) {
      cache.put(audio.getId().toString(), compute(audio));
    }
    return cache.get(audio.getId().toString());
  }

  @Override
  public void loadTheseAndForgetTheRest(List<InstrumentAudio> audios) {
    for (InstrumentAudio audio : audios) {
      if (!cache.containsKey(audio.getId().toString())) {
        try {
          cache.put(audio.getId().toString(), compute(audio));
        } catch (AudioCacheException | IOException e) {
          LOG.error("Failed to load audio into cache: {}", audio.getId(), e);
        }
      }
    }
    Set<String> audioIds = audios.stream().map(InstrumentAudio::getId).map(UUID::toString).collect(Collectors.toSet());
    for (String key : cache.keySet()) {
      if (!audioIds.contains(key)) {
        cache.remove(key);
      }
    }
  }

  @Override
  public AudioPreparedOnDisk prepare(InstrumentAudio audio) throws AudioCacheException {
    if (StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
      LOG.error("Can't load null or empty audio key! (audioRenderPathPrefix={}, demoBaseUrl={}, instrumentId={}, waveformKey={}, targetFrameRate={}, targetSampleBits={}, targetChannels={})",
        getAudioRenderPathPrefix(), projectManager.getDemoBaseUrl(), audio.getInstrumentId(), audio.getWaveformKey(), targetFrameRate, targetSampleBits, targetChannels);
      throw new AudioCacheException("Can't load null or empty audio key!");
    }

    // compute a key based on the target frame rate, sample bits, channels, and waveform key.
    String originalCachePath = projectManager.getPathToInstrumentAudio(audio.getInstrumentId());
    String resampledCachePrefix = computeResampledCachePrefix(audio.getInstrumentId(), targetFrameRate, targetSampleBits, targetChannels);
    String finalCachePath = computeResampledCachePath(resampledCachePrefix, targetFrameRate, targetSampleBits, targetChannels, audio.getWaveformKey());
    if (existsOnDisk(finalCachePath)) {
      LOG.debug("Found fully prepared audio at {}", finalCachePath);
      try {
        AudioFormat currentFormat = getAudioFormat(finalCachePath);
        return new AudioPreparedOnDisk(finalCachePath, currentFormat);
      } catch (UnsupportedAudioFileException | IOException e) {
        deleteIfExists(finalCachePath);
      }
    }

    // Create the directory if it doesn't exist
    try {
      Files.createDirectories(Path.of(resampledCachePrefix));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // If the audio format does not match, resample. FUTURE: don't increase # of channels unnecessarily
    AudioFormat currentFormat;
    try {
      currentFormat = getAudioFormat(originalCachePath);
    } catch (UnsupportedAudioFileException | IOException e) {
      deleteIfExists(originalCachePath);
      throw new RuntimeException(e);
    }

    try {
      if (!matchesAudioFormat(currentFormat, targetFrameRate, targetSampleBits, targetChannels)) {
        LOG.debug("Will resample audio file to {}Hz {}-bit {}-channel", targetFrameRate, targetSampleBits, targetChannels);
        FFmpegUtils.resampleAudio(originalCachePath, finalCachePath, targetFrameRate, targetSampleBits, targetChannels);
      } else {
        LOG.debug("Will copy audio file from {} to {}", originalCachePath, finalCachePath);
        Files.copy(Path.of(originalCachePath), Path.of(finalCachePath));
      }
      var finalFormat = getAudioFormat(finalCachePath);
      return new AudioPreparedOnDisk(finalCachePath, finalFormat);

    } catch (RuntimeException | UnsupportedAudioFileException | IOException e) {
      deleteIfExists(finalCachePath);
      throw new RuntimeException(e);
    }
  }

  @Override
  public void initialize(int targetFrameRate, int targetSampleBits, int targetChannels) {
    this.targetFrameRate = targetFrameRate;
    this.targetSampleBits = targetSampleBits;
    this.targetChannels = targetChannels;
  }

  @Override
  public void invalidateAll() {
    cache.clear();
  }

  /**
   Delete the path if it exists

   @param path to delete
   */
  private void deleteIfExists(String path) {
    try {
      Files.deleteIfExists(Path.of(path));
    } catch (IOException e) {
      LOG.error("Unable to delete file: {}", path, e);
    }
  }

  /**
   Compute the rendered audio

   @param audio audio
   @return audio prepared on disk
   @throws AudioCacheException audio cache exception
   @throws IOException         io exception
   */
  private AudioInMemory compute(InstrumentAudio audio) throws AudioCacheException, IOException {
    var fileSpec = prepare(audio);
    try {
      return audioLoader.load(audio, fileSpec.path, fileSpec.format);

    } catch (UnsupportedAudioFileException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   compute the cache path for this audio item

   @param instrumentId     instrument id
   @param targetFrameRate  target frame rate
   @param targetSampleBits target sample bits
   @param targetChannels   target channels
   @return cache path
   */
  private String computeResampledCachePrefix(UUID instrumentId, int targetFrameRate, int targetSampleBits, int targetChannels) {
    return getAudioRenderPathPrefix() + "instrument" + File.separator + instrumentId.toString() + File.separator + String.format("%d-%d-%d", targetFrameRate, targetSampleBits, targetChannels) + File.separator;
  }

  /**
   compute the cache path for this audio item

   @param resampledCachePrefix resampled cache prefix
   @param targetFrameRate      target frame rate
   @param targetSampleBits     target sample bits
   @param targetChannels       target channels
   @param waveformKey          waveform key
   @return cache path
   */
  private String computeResampledCachePath(String resampledCachePrefix, int targetFrameRate, int targetSampleBits, int targetChannels, String waveformKey) {
    return resampledCachePrefix + String.format("%d-%d-%d-%s", targetFrameRate, targetSampleBits, targetChannels, waveformKey);
  }

  /**
   @return true if this dub audio cache item exists (as audio waveform data) on disk
   */
  boolean existsOnDisk(String absolutePath) {
    return new File(absolutePath).exists();
  }

  /**
   Get the frame rate of the audio file

   @param inputAudioFilePath path to audio file
   @return frame rate of audio file
   */
  private static AudioFormat getAudioFormat(String inputAudioFilePath) throws UnsupportedAudioFileException, IOException {
    return AudioSystem.getAudioFileFormat(new File(inputAudioFilePath)).getFormat();
  }

  /**
   Whether the audio format matches the target frame rate, sample bits, and channels

   @param currentFormat    current audio format
   @param targetFrameRate  target frame rate
   @param targetSampleBits target sample bits
   @param targetChannels   target channels
   @return true if matches
   */
  private boolean matchesAudioFormat(AudioFormat currentFormat, int targetFrameRate, int targetSampleBits, int targetChannels) {
    return currentFormat.getFrameRate() == targetFrameRate
      && currentFormat.getSampleSizeInBits() == targetSampleBits

      // Mono source audio should be resampled and cached as mono
      // https://github.com/xjmusic/workstation/issues/232
      && currentFormat.getChannels() <= targetChannels;
  }

  /**
   Record of an audio file prepared on disk for caching
   */
  public record AudioPreparedOnDisk(String path, AudioFormat format) {
  }

  /**
   @return audio render path prefix
   */
  private String getAudioRenderPathPrefix() {
    return projectManager.getProjectPathPrefix() + "render" + File.separator;
  }
}
