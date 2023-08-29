package io.xj.nexus.work;

import io.xj.nexus.InputMode;
import io.xj.nexus.OutputFileMode;
import io.xj.nexus.OutputMode;

public class WorkConfiguration {
  InputMode inputMode;
  String inputTemplateKey;
  OutputFileMode outputFileMode;
  OutputMode outputMode;
  String outputPathPrefix;
  int outputSeconds;

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
}
