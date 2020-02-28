// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.exception.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.lib.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

public class ChainConfigTest  {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ChainConfig()
      .setChainId(UUID.randomUUID())
      .setType("OutputChannels")
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutChainID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Chain ID is required");

    new ChainConfig()
      .setType("OutputChannels")
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Type is required");

    new ChainConfig()
      .setChainId(UUID.randomUUID())
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("'jello' is not a valid type");

    new ChainConfig()
      .setChainId(UUID.randomUUID())
      .setType("jello")
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidValue_forNumericType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Chain OutputChannels requires numeric value!");

    new ChainConfig()
      .setChainId(UUID.randomUUID())
      .setTypeEnum(ChainConfigType.OutputChannels)
      .setValue("Not a numeric value")
      .validate();
  }

  @Test
  public void validate_failsWithInvalidValue_forTextType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Chain OutputContainer requires text value!");

    new ChainConfig()
      .setChainId(UUID.randomUUID())
      .setTypeEnum(ChainConfigType.OutputContainer)
      .setValue("75") // not a text value
      .validate();
  }

  @Test
  public void validate_failsWithoutValue() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Value is required");

    new ChainConfig()
      .setChainId(UUID.randomUUID())
      .setType("OutputChannels")
      .validate();
  }

  @Test
  public void validation_sanitizesTypeValue() throws CoreException {
    ChainConfig config;

    config = ChainConfig.create(Chain.create(), ChainConfigType.OutputSampleBits, "jangles24").setChainId(UUID.randomUUID());
    config.validate();
    assertEquals("24", config.getValue());

    config = ChainConfig.create(Chain.create(), ChainConfigType.OutputFrameRate, "48b000d").setChainId(UUID.randomUUID());
    config.validate();
    assertEquals("48000", config.getValue());

    config = ChainConfig.create(Chain.create(), ChainConfigType.OutputChannels, "2!!!!").setChainId(UUID.randomUUID());
    config.validate();
    assertEquals("2", config.getValue());

    config = ChainConfig.create(Chain.create(), ChainConfigType.OutputEncoding, "    PCM_SIGNED!!!!").setChainId(UUID.randomUUID());
    config.validate();
    assertEquals("PCM_SIGNED", config.getValue());

    config = ChainConfig.create(Chain.create(), ChainConfigType.OutputEncodingQuality, "0D.X785V  ").setChainId(UUID.randomUUID());
    config.validate();
    assertEquals("0.785", config.getValue());

    config = ChainConfig.create(Chain.create(), ChainConfigType.OutputContainer, "wav???").setChainId(UUID.randomUUID());
    config.validate();
    assertEquals("WAV", config.getValue());
  }

  @Test
  public void setValue_okToSetWhateverValueBeforeValidation() {
    assertEquals("    PCM_SIGNED!!!!", new ChainConfig().setValue("    PCM_SIGNED!!!!").getValue());
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("type", "value"), new ChainConfig().getResourceAttributeNames());
  }

}
