// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.user;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class UserWrapper {
  private User user;

  public User getUser() {
    return user;
  }

  public UserWrapper setUser(User user) {
    this.user = user;
    return this;
  }

}
