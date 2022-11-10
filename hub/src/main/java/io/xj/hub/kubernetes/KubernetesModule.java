// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.kubernetes;

import com.google.inject.AbstractModule;

public class KubernetesModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(KubernetesAdmin.class).to(KubernetesAdminImpl.class);
  }

}
