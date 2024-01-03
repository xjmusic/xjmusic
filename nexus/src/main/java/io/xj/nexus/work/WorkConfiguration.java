// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.nexus.InputMode;
import io.xj.nexus.ControlMode;

public class WorkConfiguration {
  private InputMode inputMode;
  private ControlMode controlMode;
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
  private long persistenceWindowSeconds = 3600;

  public InputMode getInputMode() {
    return inputMode;
  }

  public WorkConfiguration setInputMode(InputMode inputMode) {
    this.inputMode = inputMode;
    return this;
  }

  public ControlMode getMacroMode() {
    return controlMode;
  }

  public WorkConfiguration setMacroMode(ControlMode controlMode) {
    this.controlMode = controlMode;
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

  public long getPersistenceWindowSeconds() {
    return persistenceWindowSeconds;
  }

  public void setPersistenceWindowSeconds(long persistenceWindowSeconds) {
    this.persistenceWindowSeconds = persistenceWindowSeconds;
  }
}
