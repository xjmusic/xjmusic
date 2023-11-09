// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.nexus.InputMode;
import io.xj.nexus.MacroMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;

public class WorkConfiguration {
  private InputMode inputMode;
  private MacroMode macroMode;
  private String inputTemplateKey;
  private OutputFileMode outputFileMode;
  private OutputMode outputMode;
  private String outputPathPrefix;
  private int outputSeconds;

  private long craftAheadMicros;
  private long dubAheadMicros;
  private float outputFrameRate;
  private int outputChannels;
  private String contentStoragePathPrefix;
  private int mixBufferLengthSeconds = 10;
  private long cycleMillis = 100;
  private String tempFilePathPrefix = "/tmp/";
  private int shipOutputFileNumberDigits = 7;
  private int shipOutputPcmChunkSizeBytes = 1024;
  private long shipCycleMillis = 100;
  private long dubCycleMillis = 200;
  private long craftCycleMillis = 400;
  private long reportCycleMillis = 1000;

  public InputMode getInputMode() {
    return inputMode;
  }

  public WorkConfiguration setInputMode(InputMode inputMode) {
    this.inputMode = inputMode;
    return this;
  }

  public MacroMode getMacroMode() {
    return macroMode;
  }

  public WorkConfiguration setMacroMode(MacroMode macroMode) {
    this.macroMode = macroMode;
    return this;
  }

  public String getInputTemplateKey() {
    return inputTemplateKey;
  }

  public WorkConfiguration setInputTemplateKey(String inputTemplateKey) {
    this.inputTemplateKey = inputTemplateKey;
    return this;
  }

  public OutputFileMode getOutputFileMode() {
    return outputFileMode;
  }

  public WorkConfiguration setOutputFileMode(OutputFileMode outputFileMode) {
    this.outputFileMode = outputFileMode;
    return this;
  }

  public OutputMode getOutputMode() {
    return outputMode;
  }

  public WorkConfiguration setOutputMode(OutputMode outputMode) {
    this.outputMode = outputMode;
    return this;
  }

  public String getOutputPathPrefix() {
    return outputPathPrefix;
  }

  public WorkConfiguration setOutputPathPrefix(String outputPathPrefix) {
    this.outputPathPrefix = outputPathPrefix;
    return this;
  }

  public int getOutputSeconds() {
    return outputSeconds;
  }

  public WorkConfiguration setOutputSeconds(int outputSeconds) {
    this.outputSeconds = outputSeconds;
    return this;
  }

  public long getCraftAheadMicros() {
    return craftAheadMicros;
  }

  public WorkConfiguration setCraftAheadMicros(long craftAheadMicros) {
    this.craftAheadMicros = craftAheadMicros;
    return this;
  }

  public long getDubAheadMicros() {
    return dubAheadMicros;
  }

  public WorkConfiguration setDubAheadMicros(long dubAheadMicros) {
    this.dubAheadMicros = dubAheadMicros;
    return this;
  }

  public float getOutputFrameRate() {
    return outputFrameRate;
  }

  public WorkConfiguration setOutputFrameRate(float outputFrameRate) {
    this.outputFrameRate = outputFrameRate;
    return this;
  }

  public int getOutputChannels() {
    return outputChannels;
  }

  public WorkConfiguration setOutputChannels(int outputChannels) {
    this.outputChannels = outputChannels;
    return this;
  }

  public String getContentStoragePathPrefix() {
    return contentStoragePathPrefix;
  }

  public WorkConfiguration setContentStoragePathPrefix(String contentStoragePathPrefix) {
    this.contentStoragePathPrefix = contentStoragePathPrefix;
    return this;
  }

  public int getMixBufferLengthSeconds() {
    return mixBufferLengthSeconds;
  }

  public WorkConfiguration setMixBufferLengthSeconds(int mixBufferLengthSeconds) {
    this.mixBufferLengthSeconds = mixBufferLengthSeconds;
    return this;
  }

  public long getCycleMillis() {
    return cycleMillis;
  }

  public WorkConfiguration setCycleMillis(long cycleMillis) {
    this.cycleMillis = cycleMillis;
    return this;
  }

  public String getTempFilePathPrefix() {
    return tempFilePathPrefix;
  }

  public WorkConfiguration setTempFilePathPrefix(String tempFilePathPrefix) {
    this.tempFilePathPrefix = tempFilePathPrefix;
    return this;
  }

  public int getShipOutputFileNumberDigits() {
    return shipOutputFileNumberDigits;
  }

  public WorkConfiguration setShipOutputFileNumberDigits(int shipOutputFileNumberDigits) {
    this.shipOutputFileNumberDigits = shipOutputFileNumberDigits;
    return this;
  }

  public int getShipOutputPcmChunkSizeBytes() {
    return shipOutputPcmChunkSizeBytes;
  }

  public WorkConfiguration setShipOutputPcmChunkSizeBytes(int shipOutputPcmChunkSizeBytes) {
    this.shipOutputPcmChunkSizeBytes = shipOutputPcmChunkSizeBytes;
    return this;
  }

  public long getShipCycleMillis() {
    return shipCycleMillis;
  }

  public WorkConfiguration setShipCycleMillis(long shipCycleMillis) {
    this.shipCycleMillis = shipCycleMillis;
    return this;
  }

  public long getDubCycleMillis() {
    return dubCycleMillis;
  }

  public WorkConfiguration setDubCycleMillis(long dubCycleMillis) {
    this.dubCycleMillis = dubCycleMillis;
    return this;
  }

  public long getCraftCycleMillis() {
    return craftCycleMillis;
  }

  public WorkConfiguration setCraftCycleMillis(long craftCycleMillis) {
    this.craftCycleMillis = craftCycleMillis;
    return this;
  }

  public long getReportCycleMillis() {
    return reportCycleMillis;
  }

  public WorkConfiguration setReportCycleMillis(long reportCycleMillis) {
    this.reportCycleMillis = reportCycleMillis;
    return this;
  }
}
