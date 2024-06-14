// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model;

import io.xj.model.enums.InstrumentType;
import io.xj.model.pojos.Template;
import io.xj.model.util.ValueException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 Artist saves Template config, validate & combine with defaults. https://github.com/xjmusic/xjmusic/issues/206
 */
public class TemplateConfigTest {

  @Test
  public void setFromTemplate() throws ValueException {
    var template = new Template();
    template.setConfig("mixerCompressToAmplitude = 0.95");

    var subject = new TemplateConfig(template);

    assertEquals(0.95, subject.getMixerCompressToAmplitude(), 0.01);
  }

  @Test
  public void setFromDefaults() throws ValueException {
    var subject = new TemplateConfig(TemplateConfig.DEFAULT);

    assertEquals(1.0, subject.getMixerCompressToAmplitude(), 0.01);
  }

  @Test
  public void defaultsToString() throws ValueException {
    var subject = new TemplateConfig(TemplateConfig.DEFAULT);

    assertEquals(TemplateConfig.DEFAULT, subject.toString().replace(System.lineSeparator(), "\n"));
  }

  @Test
  public void getChoiceMuteProbability() throws ValueException {
    var subject = new TemplateConfig(TemplateConfig.DEFAULT);

    assertEquals(0.0f, subject.getChoiceMuteProbability(InstrumentType.Bass));
  }

  @Test
  public void getDubMasterVolume() throws ValueException {
    var subject = new TemplateConfig(TemplateConfig.DEFAULT);

    assertEquals(1.0f, subject.getDubMasterVolume(InstrumentType.Bass));
  }

  @Test
  public void getIntensityLayers() throws ValueException {
    var subject = new TemplateConfig(TemplateConfig.DEFAULT);

    assertEquals(1, subject.getIntensityLayers(InstrumentType.Bass));
    assertEquals(3, subject.getIntensityLayers(InstrumentType.Pad));
  }

  @Test
  public void setFromLegacyTemplate() throws ValueException {
    var template = new Template();
    template.setConfig("deltaArcBeatLayersToPrioritize = \"Drum,Bass,Pad\"");

    var subject = new TemplateConfig(template);

    assertArrayEquals(List.of("Drum", "Bass", "Pad").toArray(), subject.getDeltaArcBeatLayersToPrioritize().toArray());
  }

}
