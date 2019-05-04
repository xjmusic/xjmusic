// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.platform_message;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PlatformMessageWrapper {
  private PlatformMessage platformMessage;

  public PlatformMessage getPlatformMessage() {
    return platformMessage;
  }

  public PlatformMessageWrapper setPlatformMessage(PlatformMessage platformMessage) {
    this.platformMessage = platformMessage;
    return this;
  }
}
