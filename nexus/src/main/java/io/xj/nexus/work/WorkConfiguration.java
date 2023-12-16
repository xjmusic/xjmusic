// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.nexus.InputMode;
import io.xj.nexus.MacroMode;

public class WorkConfiguration {
  private InputMode inputMode;
  private MacroMode macroMode;
  private String inputTemplateKey;
  private int craftAheadSeconds;
  private int dubAheadSeconds;
  private int outputFrameRate;
  private int outputChannels;
  private String contentStoragePathPrefix;
  private int mixerLengthSeconds = 2;
  private String tempFilePathPrefix = "/tmp/";
  private int shipOutputFileNumberDigits = 7;
  private int shipOutputPcmChunkSizeBytes = 1024;
  private long controlCycleDelayMillis = 100;
  private long craftCycleDelayMillis = 200;
  private long dubCycleRateMillis = 200;
  private long shipCycleRateMillis = 50;
  private long persistenceWindowSeconds = 3600;

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

  public int getCraftAheadSeconds() {
    return craftAheadSeconds;
  }

  public WorkConfiguration setCraftAheadSeconds(int craftAheadSeconds) {
    this.craftAheadSeconds = craftAheadSeconds;
    return this;
  }

  public int getDubAheadSeconds() {
    return dubAheadSeconds;
  }

  public WorkConfiguration setDubAheadSeconds(int dubAheadSeconds) {
    this.dubAheadSeconds = dubAheadSeconds;
    return this;
  }

  public int getOutputFrameRate() {
    return outputFrameRate;
  }

  public WorkConfiguration setOutputFrameRate(int outputFrameRate) {
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

  public int getMixerLengthSeconds() {
    return mixerLengthSeconds;
  }

  public WorkConfiguration setMixerLengthSeconds(int mixerLengthSeconds) {
    this.mixerLengthSeconds = mixerLengthSeconds;
    return this;
  }

  public long getControlCycleDelayMillis() {
    return controlCycleDelayMillis;
  }

  public WorkConfiguration setControlCycleDelayMillis(long controlCycleDelayMillis) {
    this.controlCycleDelayMillis = controlCycleDelayMillis;
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

  public long getShipCycleRateMillis() {
    return shipCycleRateMillis;
  }

  public WorkConfiguration setShipCycleRateMillis(long shipCycleRateMillis) {
    this.shipCycleRateMillis = shipCycleRateMillis;
    return this;
  }

  public long getDubCycleRateMillis() {
    return dubCycleRateMillis;
  }

  public WorkConfiguration setDubCycleRateMillis(long dubCycleRateMillis) {
    this.dubCycleRateMillis = dubCycleRateMillis;
    return this;
  }

  public long getCraftCycleDelayMillis() {
    return craftCycleDelayMillis;
  }

  public WorkConfiguration setCraftCycleDelayMillis(long craftCycleDelayMillis) {
    this.craftCycleDelayMillis = craftCycleDelayMillis;
    return this;
  }

  public long getPersistenceWindowSeconds() {
    return persistenceWindowSeconds;
  }

  public void setPersistenceWindowSeconds(long persistenceWindowSeconds) {
    this.persistenceWindowSeconds = persistenceWindowSeconds;
  }
}
