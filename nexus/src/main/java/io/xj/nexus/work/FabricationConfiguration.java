// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.ControlMode;

public class FabricationConfiguration {
  private ControlMode controlMode;
  private Template inputTemplate;
  private int craftAheadSeconds;
  private int dubAheadSeconds;
  private int outputFrameRate;
  private int outputChannels;
  private int mixerLengthSeconds = 2;
  private int shipOutputFileNumberDigits = 7;
  private int shipOutputPcmChunkSizeBytes = 1024;
  private long persistenceWindowSeconds = 3600;

  public ControlMode getMacroMode() {
    return controlMode;
  }

  public FabricationConfiguration setMacroMode(ControlMode controlMode) {
    this.controlMode = controlMode;
    return this;
  }

  public Template getInputTemplate() {
    return inputTemplate;
  }

  public FabricationConfiguration setInputTemplate(Template inputTemplate) {
    this.inputTemplate = inputTemplate;
    return this;
  }

  public int getCraftAheadSeconds() {
    return craftAheadSeconds;
  }

  public FabricationConfiguration setCraftAheadSeconds(int craftAheadSeconds) {
    this.craftAheadSeconds = craftAheadSeconds;
    return this;
  }

  public int getDubAheadSeconds() {
    return dubAheadSeconds;
  }

  public FabricationConfiguration setDubAheadSeconds(int dubAheadSeconds) {
    this.dubAheadSeconds = dubAheadSeconds;
    return this;
  }

  public int getOutputFrameRate() {
    return outputFrameRate;
  }

  public FabricationConfiguration setOutputFrameRate(int outputFrameRate) {
    this.outputFrameRate = outputFrameRate;
    return this;
  }

  public int getOutputChannels() {
    return outputChannels;
  }

  public FabricationConfiguration setOutputChannels(int outputChannels) {
    this.outputChannels = outputChannels;
    return this;
  }

  public int getMixerLengthSeconds() {
    return mixerLengthSeconds;
  }

  public FabricationConfiguration setMixerLengthSeconds(int mixerLengthSeconds) {
    this.mixerLengthSeconds = mixerLengthSeconds;
    return this;
  }

  public int getShipOutputFileNumberDigits() {
    return shipOutputFileNumberDigits;
  }

  public FabricationConfiguration setShipOutputFileNumberDigits(int shipOutputFileNumberDigits) {
    this.shipOutputFileNumberDigits = shipOutputFileNumberDigits;
    return this;
  }

  public int getShipOutputPcmChunkSizeBytes() {
    return shipOutputPcmChunkSizeBytes;
  }

  public FabricationConfiguration setShipOutputPcmChunkSizeBytes(int shipOutputPcmChunkSizeBytes) {
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
