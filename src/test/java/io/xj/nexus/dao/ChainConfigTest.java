package io.xj.nexus.dao;

import io.xj.Chain;
import io.xj.lib.util.ValueException;
import io.xj.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 [#177355683] Artist saves Chain config, validate & combine with defaults.
 */
public class ChainConfigTest {
  ChainConfig subject;

  @Before
  public void setUp() throws ValueException {
    var config = NexusTestConfiguration.getDefault();
    subject = new ChainConfig(Chain.newBuilder()
      .setConfig("mixerCompressToAmplitude = 5.0\n" +
        "mixerCompressDecaySeconds = 0.125\n" +
        "mixerCompressRatioMax = 10.0\n" +
        "dubMasterVolumeInstrumentTypeSticky = 0.8\n" +
        "outputEncodingQuality = 0.618\n" +
        "dubMasterVolumeInstrumentTypeStripe = 0.5\n" +
        "outputSampleBits = 16\n" +
        "mixerHighpassThresholdHz = 60.0\n" +
        "mixerNormalizationMax = 0.999\n" +
        "mixerCompressAheadSeconds = 0.05\n" +
        "mixerDspBufferSize = 1024\n" +
        "dubMasterVolumeInstrumentTypeBass = 1.0\n" +
        "outputEncoding = \"PCM_SIGNED\"\n" +
        "mixerCompressRatioMin = 0.5\n" +
        "dubMasterVolumeInstrumentTypePad = 0.95\n" +
        "outputContainer = \"OGG\"\n" +
        "dubMasterVolumeInstrumentTypePercussive = 1.2\n" +
        "outputChannels = 2\n" +
        "dubMasterVolumeInstrumentTypeStab = 0.8\n" +
        "mixerLowpassThresholdHz = 6000.0\n" +
        "outputFrameRate = 48000")
      .build(), config);
  }

  @Test
  public void convertToString() {
    var result = subject.toString();

    assertEquals(
      "craftChoiceInertiaPercent = 62\n" +
        "dubMasterVolumeInstrumentTypeBass = 1.0\n" +
        "dubMasterVolumeInstrumentTypePad = 0.95\n" +
        "dubMasterVolumeInstrumentTypePercussive = 1.2\n" +
        "dubMasterVolumeInstrumentTypeStab = 0.8\n" +
        "dubMasterVolumeInstrumentTypeSticky = 0.8\n" +
        "dubMasterVolumeInstrumentTypeStripe = 0.5\n" +
        "mainProgramLengthMaxDelta = 220\n" +
        "mixerCompressAheadSeconds = 0.05\n" +
        "mixerCompressDecaySeconds = 0.125\n" +
        "mixerCompressRatioMax = 10.0\n" +
        "mixerCompressRatioMin = 0.5\n" +
        "mixerCompressToAmplitude = 5.0\n" +
        "mixerDspBufferSize = 1024\n" +
        "mixerHighpassThresholdHz = 60.0\n" +
        "mixerLowpassThresholdHz = 6000.0\n" +
        "mixerNormalizationMax = 0.999\n" +
        "outputChannels = 2\n" +
        "outputContainer = \"OGG\"\n" +
        "outputEncoding = \"PCM_SIGNED\"\n" +
        "outputEncodingQuality = 0.618\n" +
        "outputFrameRate = 48000\n" +
        "outputSampleBits = 16",
      result);
  }
}
