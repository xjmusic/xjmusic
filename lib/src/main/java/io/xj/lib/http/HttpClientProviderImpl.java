// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class HttpClientProviderImpl implements HttpClientProvider {
  private final PoolingHttpClientConnectionManager cm;

  @Autowired
  public HttpClientProviderImpl(
    @Value("${http.client.pool.max.total}")
    int httpClientPoolMaxTotal,
    @Value("${http.client.pool.max.per.route}")
    int httpClientPoolMaxPerRoute
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
