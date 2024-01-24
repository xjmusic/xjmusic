// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.hub_client;

import io.xj.hub.HubContent;
import io.xj.hub.HubContentPayload;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.UUID;

/**
 Implementation of a Hub Client for connecting to Hub and accessing contents
 */
public class HubClientImpl implements HubClient {
  static final String API_PATH_INGEST_FORMAT = "api/1/ingest/%s";
  static final String HEADER_COOKIE = "Cookie";
  final Logger LOG = LoggerFactory.getLogger(HubClientImpl.class);
  final HttpClientProvider httpClientProvider;
  final JsonProvider jsonProvider;
  final JsonapiPayloadFactory jsonapiPayloadFactory;
  final String hubAccessTokenName = "access_token";

  public HubClientImpl(
    HttpClientProvider httpClientProvider,
    JsonProvider jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory
  ) {
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
  }

  @Override
  public HubContent ingest(String baseUrl, HubClientAccess access, UUID templateId) throws HubClientException {
    CloseableHttpClient client = httpClientProvider.getClient();
    var uri = buildURI(baseUrl, String.format(API_PATH_INGEST_FORMAT, templateId.toString()));
    LOG.info("Will ingest content from {}", uri);
    try (
      CloseableHttpResponse response = client.execute(buildGetRequest(uri, access.getToken()))
    ) {
      // return content if successful.
      if (!Objects.equals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode()))
        throw buildException(uri.toString(), response);

      String json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      LOG.debug("Did ingest content; will read bytes of JSON");
      return HubContent.from(jsonProvider.getMapper().readValue(json, HubContentPayload.class));

    } catch (Exception e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public HubContent load(String shipKey, String audioBaseUrl) throws HubClientException {
    var url = String.format("%s%s.json", audioBaseUrl, shipKey);
    LOG.info("Will load content from {}", url);
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(url))
    ) {
      // return content if successful.
      if (!Objects.equals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode()))
        throw buildException(url, response);

      String json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      LOG.debug("Did load content; will read bytes of JSON");
      return HubContent.from(jsonProvider.getMapper().readValue(json, HubContentPayload.class));

    } catch (Exception e) {
      throw new HubClientException(e);
    }
  }

  /**
   Set the access token cookie header for a request to Hub

   @param uri   of request
   @param token of request
   @return http request
   */
  HttpGet buildGetRequest(URI uri, String token) {
    var request = new HttpGet(uri);
    request.setHeader(HEADER_COOKIE, String.format("%s=%s", hubAccessTokenName, token));
    return request;
  }

  /**
   Build URI for specified API path

   @param baseUrl to build URI
   @param path    to build URI
   @return URI for specified API path
   @throws HubClientException on failure to construct URI
   */
  URI buildURI(String baseUrl, String path) throws HubClientException {
    try {
      URIBuilder b = new URIBuilder(String.format("%s%s", baseUrl, path));
      return b.build();
    } catch (URISyntaxException e) {
      throw new HubClientException("Failed to construct URI", e);
    }
  }

  /**
   Log a failure message and returns a throwable exception based on a response@param uri

   @param response to log and throw
   */
  HubClientException buildException(String uri, CloseableHttpResponse response) throws HubClientException {
    throw new HubClientException(String.format("Request failed to %s\nresponse: %d %s", uri, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
  }
}
