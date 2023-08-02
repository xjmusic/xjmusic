// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.http;

import org.apache.http.impl.client.CloseableHttpClient;

/**
 * Wraps an Apache PoolingHttpClientConnectionManager to manage an HTTP connection pool.
 * <p>
 * Nexus and Ship must load all audio and metadata via CDN not direct from S3 API! https://www.pivotaltracker.com/story/show/180742075
 */
public interface HttpClientProvider {
  CloseableHttpClient getClient();
}
