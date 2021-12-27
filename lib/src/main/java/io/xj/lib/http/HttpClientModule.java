// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.http;

import com.google.inject.AbstractModule;

/**
 JSON interaction module
 <p>
 Created by Charney Kaye on 2020/06/22
 */
public class HttpClientModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(HttpClientProvider.class).to(HttpClientProviderImpl.class);
  }
}
