// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.mixer;

import io.xj.lib.util.Files;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.file.Files.deleteIfExists;

public class AudioFileWriterImpl implements AudioFileWriter {
  private static final Logger LOG = LoggerFactory.getLogger(AudioFileWriterImpl.class);
  private final AudioFormat format;
  private FileOutputStream tempFile;
  private final AtomicLong tempFileByteCount = new AtomicLong(0);
  private final AtomicReference<String> tempFilePath = new AtomicReference<>("");
  private final AtomicReference<String> outputPath = new AtomicReference<>("");

  enum FileState {
    INITIAL,
    WRITING,
    CLOSING,
    DONE
  }

  private final AtomicReference<FileState> fileState = new AtomicReference<>(FileState.INITIAL);

  public AudioFileWriterImpl(
    AudioFormat format
  ) {
    this.format = format;
  }

  @Override
  public void open(String outputPath) {
    this.outputPath.set(outputPath);
    tempFileByteCount.set(0);
    try {
      tempFilePath.set(Files.getUniqueTempFilename("stream.pcm"));
      deleteIfExists(Path.of(tempFilePath.get()));
      tempFile = new FileOutputStream(tempFilePath.get(), true);


    } catch (IOException e) {
      LOG.error("Failed to write bytes to output file!", e);
      tempFile = null;
    }
    this.fileState.set(FileState.WRITING);
  }


  @Override
  public void append(byte[] bytes) throws IOException {
    if (fileState.get() != FileState.WRITING) {
      throw new IllegalStateException("Stream is not open");
    }
    try {
      this.tempFile.write(bytes, 0, bytes.length);
      tempFile.flush();
      tempFileByteCount.addAndGet(bytes.length);
    } catch (IOException e) {
      LOG.error("Failed to write bytes to output file!", e);
      throw e;
    }
  }

  @Override
  public void close() {
    if (fileState.get() != FileState.WRITING) {
      throw new IllegalStateException("Stream is not open");
    }
    try {
      this.fileState.set(FileState.CLOSING);
      tempFile.close();
      File outputFile = new File(outputPath.get());
      var fileInputStream = FileUtils.openInputStream(new File(tempFilePath.get()));
      var bufferedInputStream = new BufferedInputStream(fileInputStream);
      AudioInputStream ais = new AudioInputStream(bufferedInputStream, format, tempFileByteCount.get());
      AudioSystem.write(ais, AudioFileFormat.Type.WAVE, outputFile);
      this.fileState.set(FileState.DONE);
      LOG.info("Did write {} bytes of PCM data to output WAV container {}", tempFileByteCount.get(), outputPath.get());
    } catch (IOException e) {
      LOG.error("Failed to close output file stream!", e);
    }
  }

  @Override
  public boolean isWriting() {
    return fileState.get() == FileState.WRITING;
  }
}
