// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.http;


import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;

public class HttpClientProviderImpl implements HttpClientProvider {
  final PoolingHttpClientConnectionManager cm;
  final int httpClientPoolMaxTotal = 200;
  final int httpClientPoolMaxPerRoute = 20;

  public HttpClientProviderImpl() {
    cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(httpClientPoolMaxTotal);
    cm.setDefaultMaxPerRoute(httpClientPoolMaxPerRoute);
  }

  @Override
  public CloseableHttpClient getClient() {
    return HttpClients.custom().setConnectionManager(cm).build();
  }

}
