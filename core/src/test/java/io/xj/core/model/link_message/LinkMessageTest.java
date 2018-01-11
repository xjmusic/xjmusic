// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.link_message;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class LinkMessageTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new LinkMessage()
      .setType("Warning")
      .setLinkId(BigInteger.valueOf(2))
      .setBody("This is a warning")
      .validate();
  }

  @Test
  public void validate_failsWithoutLinkID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Link ID is required");

    new LinkMessage()
      .setType("Warning")
      .setBody("This is a warning")
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new LinkMessage()
      .setLinkId(BigInteger.valueOf(2))
      .setBody("This is a warning")
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("not a valid type");

    new LinkMessage()
      .setType("sneeze")
      .setLinkId(BigInteger.valueOf(2))
      .setBody("This is a warning")
      .validate();
  }

  @Test
  public void validate_failsWithoutBody() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Body is required");

    new LinkMessage()
      .setType("Warning")
      .setLinkId(BigInteger.valueOf(2))
      .validate();
  }

}
