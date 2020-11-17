// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.entity.Entities;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.PayloadFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 Implementation of a Hub Client for connecting to Hub and accessing contents
 */
public class HubClientImpl implements HubClient {
  private static final String API_PATH_INGEST = "ingest";
  private static final String API_PATH_AUTH = "auth";
  private static final String HEADER_COOKIE = "Cookie";
  private final CloseableHttpClient httpClient;
  private final String baseUrl;
  private final String tokenName;
  private final Logger log = LoggerFactory.getLogger(HubClientImpl.class);
  private final PayloadFactory payloadFactory;
  private final String internalToken;

  @Inject
  public HubClientImpl(
    Config config,
    PayloadFactory payloadFactory
  ) {
    this.payloadFactory = payloadFactory;
    httpClient = HttpClients.createDefault();

    tokenName = config.getString("access.tokenName");
    baseUrl = config.getString("hub.baseUrl");
    internalToken = config.getString("hub.internalToken");
  }

  @Override
  public HubContent ingest(HubClientAccess access, Set<String> libraryIds, Set<String> programIds, Set<String> instrumentIds) throws HubClientException {
    try {
      HttpGet request = new HttpGet(buildURI(HubClientImpl.API_PATH_INGEST, ImmutableMap.of(
        "libraryIds", Entities.csvOf(libraryIds),
        "programIds", Entities.csvOf(programIds),
        "instrumentIds", Entities.csvOf(instrumentIds)
      )));
      setAccessCookie(request, internalToken);
      CloseableHttpResponse response = httpClient.execute(request);

      // return content if successful
      if (Objects.equals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode())) {
        String entity = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
        Payload payload = payloadFactory.deserialize(entity);
        return new HubContent(payloadFactory.toMany(payload));
      }

      // if we got here, it's a failure
      log.error("Failed to request {} because {}", request.getURI(), response);
      throw new HubClientException(String.format("Failed to request %s because %s",
        request.getURI(), response.getStatusLine().getReasonPhrase()));

    } catch (IOException | JsonApiException e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public HubClientAccess auth(String accessToken) throws HubClientException {
    HttpGet request = new HttpGet(buildURI(HubClientImpl.API_PATH_AUTH, ImmutableMap.of()));
    request.setHeader(HEADER_COOKIE, String.format("%s=%s", tokenName, accessToken));
    setAccessCookie(request, accessToken);
    HubClientAccess access;
    CloseableHttpResponse response;
    try {
      response = httpClient.execute(request);
      access = new ObjectMapper().readValue(response.getEntity().getContent(), HubClientAccess.class);
    } catch (IOException e) {
      throw new HubClientException("Failed to authenticate with Hub API", e);
    }
    return access;
  }

  /**
   Set the access token cookie header for a request to Hub

   @param request     to set cookie header for
   @param accessToken to set
   */
  private void setAccessCookie(HttpGet request, String accessToken) {
    request.setHeader(HEADER_COOKIE, String.format("%s=%s", tokenName, accessToken));
  }

  /**
   Build URI for specified API path

   @param path        to build URI to
   @param queryParams to include with path
   @return URI for specified API path
   @throws HubClientException on failure to construct URI
   */
  private URI buildURI(String path, Map<String, String> queryParams) throws HubClientException {
    try {
      URIBuilder b = new URIBuilder(String.format("%s%s", baseUrl, path));
      queryParams.forEach(b::addParameter);
      return b.build();
    } catch (URISyntaxException e) {
      throw new HubClientException("Failed to construct URI", e);
    }
  }

}
