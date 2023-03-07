// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.http;

import io.xj.lib.app.AppEnvironment;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HttpClientProviderImpl implements HttpClientProvider {
  private final PoolingHttpClientConnectionManager cm;

  @Autowired
  public HttpClientProviderImpl(
    AppEnvironment env
  ) {
    cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(env.getHttpClientPoolMaxTotal());
    cm.setDefaultMaxPerRoute(env.getHttpClientPoolMaxPerRoute());
  }

  @Override
  public CloseableHttpClient getClient() {
    return HttpClients.custom()
      .setConnectionManager(cm)
      .build();
  }

}
