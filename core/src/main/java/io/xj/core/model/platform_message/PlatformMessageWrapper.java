// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.platform_message;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.entity.EntityWrapper;

import java.util.Objects;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PlatformMessageWrapper extends EntityWrapper {

  // Choice
  private PlatformMessage platformMessage;

  public PlatformMessage getPlatformMessage() {
    return platformMessage;
  }

  public PlatformMessageWrapper setPlatformMessage(PlatformMessage platformMessage) {
    this.platformMessage = platformMessage;
    return this;
  }

  /**
   Validate data.

   @throws BusinessException if invalid.
   */
  @Override
  public PlatformMessage validate() throws BusinessException {
    if (Objects.isNull(platformMessage)) {
      throw new BusinessException("Platform Message is required.");
    }
    platformMessage.validate();
    return platformMessage;
  }

}
