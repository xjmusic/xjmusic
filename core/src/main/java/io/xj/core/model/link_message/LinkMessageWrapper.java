// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.link_message;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.EntityWrapper;

import java.util.Objects;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class LinkMessageWrapper extends EntityWrapper {

  // Choice
  private LinkMessage linkMessage;

  public LinkMessage getLinkMessage() {
    return linkMessage;
  }

  public LinkMessageWrapper setLinkMessage(LinkMessage linkMessage) {
    this.linkMessage = linkMessage;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public LinkMessage validate() throws BusinessException {
    if (Objects.isNull(linkMessage)) {
      throw new BusinessException("Link Message is required.");
    }
    linkMessage.validate();
    return linkMessage;
  }

}
