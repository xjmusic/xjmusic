// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.http;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

/**
 Wraps an Apache PoolingHttpClientConnectionManager to manage an HTTP connection pool.
 */
public interface HttpClientProvider {
  CloseableHttpClient getClient();
}
