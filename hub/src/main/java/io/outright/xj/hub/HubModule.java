// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub;

import com.google.inject.AbstractModule;
import io.outright.xj.hub.controller.account.AccountController;
import io.outright.xj.hub.controller.account.AccountControllerImpl;
import io.outright.xj.hub.controller.auth.AuthController;
import io.outright.xj.hub.controller.auth.GoogleAuthController;
import io.outright.xj.hub.controller.user.UserController;
import io.outright.xj.hub.controller.user.UserControllerImpl;

public class HubModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(UserController.class).to(UserControllerImpl.class);
    bind(AccountController.class).to(AccountControllerImpl.class);
    bind(AuthController.class).to(GoogleAuthController.class);
  }
}
