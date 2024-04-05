// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.hub_client;

import io.xj.hub.HubContent;
import io.xj.hub.HubUploadAuthorization;
import io.xj.nexus.project.ProjectAudioUpload;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;

import java.util.UUID;

/**
 * Interface of a Hub Client for connecting to Hub and accessing contents
 */
public interface HubClientFactory {
  long FILE_SIZE_NOT_FOUND = -404;

  /**
   * Load shipped content from a static file in API v1 (HubContentPayload a.k.a. JSONAPI)
   * <p>
   * Nexus production fabrication from static source (without Hub) https://www.pivotaltracker.com/story/show/177020318
   *
   * @param httpClient   to use
   * @param shipKey      to load
   * @param audioBaseUrl to use for audio
   * @return hub content
   */
  HubContent loadApiV1(CloseableHttpClient httpClient, String shipKey, String audioBaseUrl) throws HubClientException;

  /**
   * Download a file from the given URL to the given output path, retrying some number of times
   *
   * @param httpClient http client (don't close the client; only close the responses from it)
   * @param url        URL to download from
   * @param outputPath path to write to
   * @return true if the file was downloaded successfully
   */
  boolean downloadRemoteFileWithRetry(CloseableHttpClient httpClient, String url, String outputPath, long expectedSize);

  /**
   * Get the size of the file at the given URL
   *
   * @param httpClient http client (don't close the client; only close the responses from it)
   * @param url        url
   * @return size of the file
   * @throws Exception if the file size could not be determined
   */
  long getRemoteFileSize(CloseableHttpClient httpClient, String url) throws Exception;
}
