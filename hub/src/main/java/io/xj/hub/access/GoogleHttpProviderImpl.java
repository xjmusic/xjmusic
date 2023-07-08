// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.api.client.http.HttpTransport;
import org.springframework.stereotype.Service;

@Service
class GoogleHttpProviderImpl implements GoogleHttpProvider {
  final HttpTransport httpTransport;

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
