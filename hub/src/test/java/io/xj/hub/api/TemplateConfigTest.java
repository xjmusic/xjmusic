//  Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import io.xj.hub.TemplateConfig;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.util.ValueException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 [#177355683] Artist saves Template config, validate & combine with defaults.
 */
public class TemplateConfigTest {
  TemplateConfig subject;

  @Before
  public void setUp() throws ValueException {
    var template = new Template();
    template.setConfig(
      "choiceDeltaEnabled = true\n" +
        "dubMasterVolumeInstrumentTypeBass = 1.0\n" +
        "dubMasterVolumeInstrumentTypePad = 0.95\n" +
        "dubMasterVolumeInstrumentTypePercussive = 1.2\n" +
        "dubMasterVolumeInstrumentTypeStab = 0.8\n" +
        "dubMasterVolumeInstrumentTypeSticky = 0.8\n" +
        "dubMasterVolumeInstrumentTypeStripe = 0.5\n" +
        "mixerCompressAheadSeconds = 0.05\n" +
        "mixerCompressDecaySeconds = 0.125\n" +
        "mixerCompressRatioMax = 10.0\n" +
        "mixerCompressRatioMin = 0.5\n" +
        "mixerCompressToAmplitude = 5.0\n" +
        "mixerDspBufferSize = 1024\n" +
        "mixerHighpassThresholdHz = 60.0\n" +
        "mixerLowpassThresholdHz = 6000.0\n" +
        "mixerNormalizationCeiling = 0.999\n" +
        "outputChannels = 2\n" +
        "outputContainer = \"OGG\"\n" +
        "outputEncoding = \"PCM_SIGNED\"\n" +
        "outputEncodingQuality = 0.618\n" +
        "outputFrameRate = 48000\n" +
        "outputSampleBits = 16\n"
    );
    subject = new TemplateConfig(template);
  }

  @Test
  public void convertToString() {
    var result = subject.toString();

    assertTrue(result.contains("choiceDeltaEnabled"));
  }
}
