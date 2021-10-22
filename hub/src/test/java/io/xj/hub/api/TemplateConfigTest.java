// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import io.xj.hub.TemplateConfig;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.util.ValueException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 [#177355683] Artist saves Template config, validate & combine with defaults.
 */
public class TemplateConfigTest {

  @Test
  public void setFromTemplate() throws ValueException {
    var template = new Template();
    template.setConfig("deltaArcDetailPlateauRatio = 0.95");

    var subject = new TemplateConfig(template);

    assertEquals(0.95, subject.getDeltaArcDetailPlateauRatio(), 0.01);
  }

  @Test
  public void setFromDefaults() throws ValueException {
    var subject = new TemplateConfig(TemplateConfig.DEFAULT);

    assertEquals(0.38, subject.getDeltaArcDetailPlateauRatio(), 0.01);
  }

  @Test
  public void defaultsToString() throws ValueException {
    var subject = new TemplateConfig(TemplateConfig.DEFAULT);

    assertEquals(TemplateConfig.DEFAULT, subject.toString());
  }

}
