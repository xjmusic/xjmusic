// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.hub.pojos.Template;
import io.xj.nexus.ControlMode;

public class FabricationSettings {
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

  public FabricationSettings setMacroMode(ControlMode controlMode) {
    this.controlMode = controlMode;
    return this;
  }

  public Template getInputTemplate() {
    return inputTemplate;
  }

  public FabricationSettings setInputTemplate(Template inputTemplate) {
    this.inputTemplate = inputTemplate;
    return this;
  }

  public int getCraftAheadSeconds() {
    return craftAheadSeconds;
  }

  public FabricationSettings setCraftAheadSeconds(int craftAheadSeconds) {
    this.craftAheadSeconds = craftAheadSeconds;
    return this;
  }

  public int getDubAheadSeconds() {
    return dubAheadSeconds;
  }

  public FabricationSettings setDubAheadSeconds(int dubAheadSeconds) {
    this.dubAheadSeconds = dubAheadSeconds;
    return this;
  }

  public int getOutputFrameRate() {
    return outputFrameRate;
  }

  public FabricationSettings setOutputFrameRate(int outputFrameRate) {
    this.outputFrameRate = outputFrameRate;
    return this;
  }

  public int getOutputChannels() {
    return outputChannels;
  }

  public FabricationSettings setOutputChannels(int outputChannels) {
    this.outputChannels = outputChannels;
    return this;
  }

  public int getMixerLengthSeconds() {
    return mixerLengthSeconds;
  }

  public FabricationSettings setMixerLengthSeconds(int mixerLengthSeconds) {
    this.mixerLengthSeconds = mixerLengthSeconds;
    return this;
  }

  public int getShipOutputFileNumberDigits() {
    return shipOutputFileNumberDigits;
  }

  public FabricationSettings setShipOutputFileNumberDigits(int shipOutputFileNumberDigits) {
    this.shipOutputFileNumberDigits = shipOutputFileNumberDigits;
    return this;
  }

  public int getShipOutputPcmChunkSizeBytes() {
    return shipOutputPcmChunkSizeBytes;
  }

  public FabricationSettings setShipOutputPcmChunkSizeBytes(int shipOutputPcmChunkSizeBytes) {
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
