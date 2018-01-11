// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.external.google;

import com.google.api.client.http.HttpTransport;
import com.google.inject.Inject;

public class GoogleHttpProviderImpl implements GoogleHttpProvider {
  private HttpTransport httpTransport;

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
