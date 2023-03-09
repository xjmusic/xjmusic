// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import io.xj.hub.TemplateConfig;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.util.ValueException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 Artist saves Template config, validate & combine with defaults. https://www.pivotaltracker.com/story/show/177355683
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

    assertEquals(TemplateConfig.DEFAULT, subject.toString());
  }

}
