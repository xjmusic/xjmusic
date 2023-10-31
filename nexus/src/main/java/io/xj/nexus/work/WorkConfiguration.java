// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.work;

import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;

public class WorkConfiguration {
  private InputMode inputMode;
  private String inputTemplateKey;
  private OutputFileMode outputFileMode;
  private OutputMode outputMode;
  private String outputPathPrefix;
  private int outputSeconds;

  private int craftAheadSeconds;
  private int dubAheadSeconds;
  private int shipAheadSeconds;
  private double outputFrameRate;
  private int outputChannels;
  private String contentStoragePathPrefix;

  public InputMode getInputMode() {
    return inputMode;
  }

  public WorkConfiguration setInputMode(InputMode inputMode) {
    this.inputMode = inputMode;
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

  public int getShipAheadSeconds() {
    return shipAheadSeconds;
  }

  public WorkConfiguration setShipAheadSeconds(int shipAheadSeconds) {
    this.shipAheadSeconds = shipAheadSeconds;
    return this;
  }

  public double getOutputFrameRate() {
    return outputFrameRate;
  }

  public WorkConfiguration setOutputFrameRate(double outputFrameRate) {
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
}
