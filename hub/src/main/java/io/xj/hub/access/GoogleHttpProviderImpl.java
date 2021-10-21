// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.api.client.http.HttpTransport;
import com.google.inject.Inject;

class GoogleHttpProviderImpl implements GoogleHttpProvider {
  private final HttpTransport httpTransport;

  @Inject
  public GoogleHttpProviderImpl(
    HttpTransport httpTransport
  ) {
    this.httpTransport = httpTransport;
  }

  @Override
  public HttpTransport getTransport() {
    return httpTransport;
  }
}
