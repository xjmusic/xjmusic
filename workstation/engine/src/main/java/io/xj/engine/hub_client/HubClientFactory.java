// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.hub_client;

import io.xj.hub.HubContent;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

/**
 Interface of a Hub Client for connecting to Hub and accessing contents
 */
public interface HubClientFactory {
  long FILE_SIZE_NOT_FOUND = -404;

  /**
   Load shipped content from a static file in API v1 (HubContentPayload a.k.a. JSONAPI)
   <p>
   Production fabrication from static source (without Hub) https://github.com/xjmusic/workstation/issues/271

   @param httpClient  to use
   @param baseUrl     to use for audio
   @param templateKey to load
   @return hub content
   */
  HubContent loadApiV1(CloseableHttpClient httpClient, String baseUrl, String templateKey) throws HubClientException;

  /**
   Download a file from the given URL to the given output path, retrying some number of times

   @param httpClient http client (don't close the client; only close the responses from it)
   @param url        URL to download from
   @param outputPath path to write to
   @return true if the file was downloaded successfully
   */
  boolean downloadRemoteFileWithRetry(CloseableHttpClient httpClient, String url, String outputPath, long expectedSize);

  /**
   Get the size of the file at the given URL

   @param httpClient http client (don't close the client; only close the responses from it)
   @param url        url
   @return size of the file
   @throws Exception if the file size could not be determined
   */
  long getRemoteFileSize(CloseableHttpClient httpClient, String url) throws Exception;
}
