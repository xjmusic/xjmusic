// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub;

import io.outright.xj.hub.controller.auth.AuthController;
import io.outright.xj.hub.controller.auth.GoogleAuthController;
import io.outright.xj.hub.controller.user.UserController;
import io.outright.xj.hub.controller.user.UserControllerImpl;

import com.google.inject.AbstractModule;

public class HubModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(UserController.class).to(UserControllerImpl.class);
    bind(AuthController.class).to(GoogleAuthController.class);
  }
}
