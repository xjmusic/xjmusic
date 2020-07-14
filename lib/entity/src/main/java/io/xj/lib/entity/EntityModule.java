// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import com.google.inject.AbstractModule;

/**
 Injection module for Entity module
 <p>
 Created by Charney Kaye on 2020/05/06
 */
public class EntityModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(EntityFactory.class).to(EntityFactoryImpl.class);
  }

}
