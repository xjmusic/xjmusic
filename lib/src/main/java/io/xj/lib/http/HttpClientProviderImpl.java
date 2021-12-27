// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.http;

import com.google.inject.Inject;
import io.xj.lib.app.Environment;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class HttpClientProviderImpl implements HttpClientProvider {
  private final PoolingHttpClientConnectionManager cm;

  @Inject
  public HttpClientProviderImpl(
    Environment env
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
