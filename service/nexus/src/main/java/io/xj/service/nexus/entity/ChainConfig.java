package io.xj.service.nexus.entity;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import io.xj.lib.util.ValueException;

import javax.sound.sampled.AudioFormat;

/**
 Parse a TypeSafe `config` value for a Chain's configuration, overriding values from top-level default.conf--
 e.g.
 if the `config` value contains only `previewLengthMaxHours = 8`
 */
public class ChainConfig {
  private final int outputChannels;
  private final String outputContainer;
  private final AudioFormat.Encoding outputEncoding;
  private final double outputEncodingQuality;
  private final int outputFrameRate;
  private final int outputSampleBits;

  /**
   Instantiate a Chain configuration from a string of typesafe config.
   Said string will be embedded in a `chain{...}` block such that
   provided simple Key=Value pairs will be understood as members of `chain`
   e.g. will override values from the `chain{...}` block of the top-level **default.conf**

   @param chain to get Config from
   */
  public ChainConfig(Chain chain, Config defaultConfig) throws ValueException {
    try {
      Config config = Strings.isNullOrEmpty(chain.getConfig()) ?
        defaultConfig :
        ConfigFactory.parseString(String.format("chain {\n%s\n}", chain.getConfig()))
          .withFallback(defaultConfig);
      outputChannels = config.getInt("chain.outputChannels");
      outputContainer = config.getString("chain.outputContainer");
      outputEncoding = new AudioFormat.Encoding(config.getString("chain.outputEncoding"));
      outputEncodingQuality = config.getDouble("chain.outputEncodingQuality");
      outputFrameRate = config.getInt("chain.outputFrameRate");
      outputSampleBits = config.getInt("chain.outputSampleBits");

    } catch (ConfigException e) {
      throw new ValueException(e.getMessage());
    }
  }

  /**
   @return Output Sample Bits
   */
  public int getOutputSampleBits() {
    return outputSampleBits;
  }

  /**
   @return Output Frame Rate (Hz)
   */
  public int getOutputFrameRate() {
    return outputFrameRate;
  }

  /**
   @return Output Encoding Quality (ratio from 0 to 1)
   */
  public double getOutputEncodingQuality() {
    return outputEncodingQuality;
  }

  /**
   @return Output Encoding
   */
  public AudioFormat.Encoding getOutputEncoding() {
    return outputEncoding;
  }

  /**
   @return Output Container
   */
  public String getOutputContainer() {
    return outputContainer;
  }

  /**
   @return # of Output Channels
   */
  public int getOutputChannels() {
    return outputChannels;
  }
}
