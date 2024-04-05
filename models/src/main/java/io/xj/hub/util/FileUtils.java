// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.util;

import io.xj.hub.music.Accidental;
import io.xj.hub.pojos.InstrumentAudio;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 Get a temp file
 */
public interface FileUtils {
  /**
   Assert size of two different files is within a tolerated threshold

   @param f1        to compare
   @param f2        to compare
   @param tolerance to allow
   @return true if within tolerance
   */
  static boolean isFileSizeWithin(File f1, File f2, float tolerance) {
    float deviance = (float) f1.getTotalSpace() / f2.getTotalSpace();
    return (1 - tolerance) < deviance && (1 + tolerance) > deviance;
  }

  /**
   get the content of a file from java resources

   @param filePath to get
   @return File
   */
  static String getFileContent(String filePath) throws IOException {
    return new String(new BufferedInputStream(new FileInputStream(filePath)).readAllBytes());
  }

  /**
   get the content of a file from java resources

   @param filePath to get
   @return File
   */
  static String getResourceFileContent(String filePath) throws IOException {
    return new String(new BufferedInputStream(new FileInputStream(getResourceFile(filePath))).readAllBytes());
  }

  /**
   get a file from java resources

   @param filePath to get
   @return File
   */
  static File getResourceFile(String filePath) {
    InternalResource internalResource = new InternalResource(filePath);
    return internalResource.getFile();
  }

  /**
   Reads given resource file as a string.

   @param fileName path to the resource file
   @return the file's contents
   @throws IOException if read fails for any reason
   */
  static String readResourceFileAsString(String fileName) throws IOException {
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    try (InputStream is = classLoader.getResourceAsStream(fileName)) {
      if (is == null) return null;
      try (InputStreamReader isr = new InputStreamReader(is);
           BufferedReader reader = new BufferedReader(isr)) {
        return reader.lines().collect(Collectors.joining(System.lineSeparator()));
      }
    }
  }

  /**
   Compute the waveform key for an instrument audio

   @param projectName    to which the library belongs
   @param libraryName    to which the instrument belongs
   @param instrumentName to which the audio belongs
   @param audio          for which to compute the key
   @param extension      of the audio file
   @return the waveform key
   */
  static String computeWaveformKey(String projectName, String libraryName, String instrumentName, InstrumentAudio audio, String extension) {
    return String.format("%s.%s",
      Stream.of(
          StringUtils.toAlphanumericHyphenated(projectName),
          StringUtils.toAlphanumericHyphenated(libraryName),
          StringUtils.toAlphanumericHyphenated(instrumentName),
          StringUtils.toAlphanumericHyphenated(Accidental.replaceWithExplicit(audio.getName())),
          StringUtils.toAlphanumericHyphenated(Accidental.replaceWithExplicit(audio.getTones()))
        ).filter(StringUtils::isNotNullOrEmpty)
        .collect(Collectors.joining("-")),
      extension);
  }
}
