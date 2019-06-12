//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.sub.ChainConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

public class ChainConfigTest extends CoreTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ChainConfig()
      .setChainId(BigInteger.valueOf(974L))
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
      .setChainId(BigInteger.valueOf(974L))
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("'jello' is not a valid type");

    new ChainConfig()
      .setChainId(BigInteger.valueOf(974L))
      .setType("jello")
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidValue_forNumericType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Chain OutputChannels requires numeric value!");

    new ChainConfig()
      .setChainId(BigInteger.valueOf(974L))
      .setTypeEnum(ChainConfigType.OutputChannels)
      .setValue("Not a numeric value")
      .validate();
  }

  @Test
  public void validate_failsWithInvalidValue_forTextType() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Chain OutputContainer requires text value!");

    new ChainConfig()
      .setChainId(BigInteger.valueOf(974L))
      .setTypeEnum(ChainConfigType.OutputContainer)
      .setValue("75") // not a text value
      .validate();
  }

  @Test
  public void validate_failsWithoutValue() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Value is required");

    new ChainConfig()
      .setChainId(BigInteger.valueOf(974L))
      .setType("OutputChannels")
      .validate();
  }

  @Test
  public void validation_sanitizesTypeValue() throws CoreException {
    assertEquals("24", newChainConfig(ChainConfigType.OutputSampleBits, "jangles24").setChainId(BigInteger.valueOf(7)).validate().getValue());
    assertEquals("48000", newChainConfig(ChainConfigType.OutputFrameRate, "48b000d").setChainId(BigInteger.valueOf(7)).validate().getValue());
    assertEquals("2", newChainConfig(ChainConfigType.OutputChannels, "2!!!!").setChainId(BigInteger.valueOf(7)).validate().getValue());
    assertEquals("PCM_SIGNED", newChainConfig(ChainConfigType.OutputEncoding, "    PCM_SIGNED!!!!").setChainId(BigInteger.valueOf(7)).validate().getValue());
    assertEquals("0.785", newChainConfig(ChainConfigType.OutputEncodingQuality, "0D.X785V  ").setChainId(BigInteger.valueOf(7)).validate().getValue());
    assertEquals("WAV", newChainConfig(ChainConfigType.OutputContainer, "wav???").setChainId(BigInteger.valueOf(7)).validate().getValue());
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
