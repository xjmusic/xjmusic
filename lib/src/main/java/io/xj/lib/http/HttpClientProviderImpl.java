// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientProviderImpl implements HttpClientProvider {
  final PoolingHttpClientConnectionManager cm;
  final int httpClientPoolMaxTotal = 200;
  final int httpClientPoolMaxPerRoute = 20;

  public HttpClientProviderImpl(
  ) {
    cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(httpClientPoolMaxTotal);
    cm.setDefaultMaxPerRoute(httpClientPoolMaxPerRoute);
  }

  @Override
  public CloseableHttpClient getClient() {
    return HttpClients.custom()
      .setConnectionManager(cm)
      .build();
  }

}
