// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.link_message;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

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
    if (this.linkMessage == null) {
      throw new BusinessException("Choice is required.");
    }
    this.linkMessage.validate();
    return this.linkMessage;
  }

}
