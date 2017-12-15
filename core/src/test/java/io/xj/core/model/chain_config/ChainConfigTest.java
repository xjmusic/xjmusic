// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_config;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class ChainConfigTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setType("OutputChannels")
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutChainID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    new ChainConfig()
      .setType("OutputChannels")
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'jello' is not a valid type");

    new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setType("jello")
      .setValue(String.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutValue() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Value is required");

    new ChainConfig()
      .setChainId(BigInteger.valueOf(974))
      .setType("OutputChannels")
      .validate();
  }

}
