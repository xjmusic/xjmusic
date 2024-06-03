// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.hub_client;

import io.xj.hub.HubContent;
import io.xj.hub.HubContentPayload;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.engine.FabricationException;
import io.xj.engine.http.HttpClientProvider;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 Implementation of a Hub Client for connecting to Hub and accessing contents
 */
public class HubClientFactoryImpl implements HubClientFactory {
  private final int audioDownloadRetries;
  final Logger LOG = LoggerFactory.getLogger(HubClientFactoryImpl.class);
  final HttpClientProvider httpClientProvider;
  final JsonProvider jsonProvider;
  final JsonapiPayloadFactory jsonapiPayloadFactory;

  public HubClientFactoryImpl(
    HttpClientProvider httpClientProvider,
    JsonProvider jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    int audioDownloadRetries
  ) {
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.audioDownloadRetries = audioDownloadRetries;
  }

  @Override
  public HubContent loadApiV1(CloseableHttpClient httpClient, String baseUrl, String templateKey) throws HubClientException {
    var url = String.format("%s%s.json", baseUrl, templateKey);
    LOG.info("Will load content from {}", url);
    //noinspection deprecation
    try (
      CloseableHttpResponse response = httpClient.execute(new HttpGet(url))
    ) {
      // return content if successful.
      if (!Objects.equals(HttpStatus.SC_OK, response.getCode()))
        throw buildException(url, response);

      String json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      LOG.debug("Did load content; will read bytes of JSON");
      var content = HubContent.from(jsonProvider.getMapper().readValue(json, HubContentPayload.class));
      content.setDemo(true);
      return content;

    } catch (Exception e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public boolean downloadRemoteFileWithRetry(CloseableHttpClient httpClient, String url, String outputPath, long expectedSize) {
    for (int attempt = 1; attempt <= audioDownloadRetries; attempt++) {
      try {
        Path path = Paths.get(outputPath);
        Files.deleteIfExists(path);
        downloadRemoteFile(httpClient, url, outputPath);
        long downloadedSize = Files.size(path);
        if (downloadedSize == expectedSize) {
          return true;
        }
        LOG.info("File size does not match! Attempt " + attempt + " of " + audioDownloadRetries + " to download " + url + " to " + outputPath + " failed. Expected " + expectedSize + " bytes, but got " + downloadedSize + " bytes.");

      } catch (Exception e) {
        LOG.info("Attempt " + attempt + " of " + audioDownloadRetries + " to download " + url + " to " + outputPath + " failed because " + e.getMessage());
      }
    }
    return false;
  }

  /**
   Download a file from the given URL to the given output path

   @param httpClient http client (don't close the client; only close the responses from it)
   @param url        url
   @param outputPath output path
   */
  private void downloadRemoteFile(CloseableHttpClient httpClient, String url, String outputPath) throws IOException, FabricationException {
    //noinspection deprecation
    try (
      CloseableHttpResponse response = httpClient.execute(new HttpGet(url))
    ) {
      if (Objects.isNull(response.getEntity().getContent()))
        throw new FabricationException(String.format("Unable to write bytes to disk: %s", outputPath));

      try (OutputStream toFile = FileUtils.openOutputStream(new File(outputPath))) {
        var size = IOUtils.copy(response.getEntity().getContent(), toFile); // stores number of bytes copied
        LOG.debug("Did write media item to disk: {} ({} bytes)", outputPath, size);
      }
    }
  }

  @Override
  public long getRemoteFileSize(CloseableHttpClient httpClient, String url) throws Exception {
    //noinspection deprecation
    try (
      CloseableHttpResponse response = httpClient.execute(new HttpHead(url))
    ) {
      if (response.getCode() == HttpStatus.SC_NOT_FOUND) {
        return FILE_SIZE_NOT_FOUND;
      }
      var contentLengthHeader = response.getFirstHeader("Content-Length");
      if (Objects.isNull(contentLengthHeader)) {
        throw new FabricationException(String.format("No Content-Length header found: %s", url));
      }
      return Long.parseLong(contentLengthHeader.getValue());
    } catch (Exception e) {
      throw new FabricationException(String.format("Unable to get %s", url), e);
    }
  }

  /**
   Log a failure message and returns a throwable exception based on a response@param uri

   @param response to log and throw
   */
  HubClientException buildException(String uri, CloseableHttpResponse response) throws HubClientException {
    throw new HubClientException(String.format("Request failed to %s\nresponse: %d %s", uri, response.getCode(), response.getReasonPhrase()));
  }
}
