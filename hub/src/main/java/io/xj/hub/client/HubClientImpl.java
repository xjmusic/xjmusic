// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.hub.ingest.HubContentPayload;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.Environment;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Implementation of a Hub Client for connecting to Hub and accessing contents
 */
@Singleton
public class HubClientImpl implements HubClient {
  private static final String API_PATH_INGEST_FORMAT = "api/1/ingest/%s";
  private static final String API_PATH_USER_TEMPLATE_PLAYBACK_FORMAT = "api/1/templates/playing?userId=%s";
  private static final String HEADER_COOKIE = "Cookie";
  private final Logger LOG = LoggerFactory.getLogger(HubClientImpl.class);
  private final String ingestUrl;
  private final String ingestTokenName;
  private final HttpClientProvider httpClientProvider;
  private final JsonProviderImpl jsonProvider;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final String ingestTokenValue;
  private final String audioBaseUrl;

  @Inject
  public HubClientImpl(
    Environment env,
    HttpClientProvider httpClientProvider,
    JsonProviderImpl jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory
  ) {
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;

    ingestUrl = env.getIngestURL();
    ingestTokenName = env.getIngestTokenName();
    ingestTokenValue = env.getIngestTokenValue();
    audioBaseUrl = env.getAudioBaseUrl();

    String obscuredSecret = Arrays.stream(ingestTokenValue.split("")).map(c -> "*").collect(Collectors.joining());
    LOG.info("Will connect to Hub at {} with token '{}' value '{}'", ingestUrl, ingestTokenName, obscuredSecret);
  }

  @Override
  public HubContent ingest(HubClientAccess access, UUID templateId) throws HubClientException {
    CloseableHttpClient client = httpClientProvider.getClient();
    var uri = buildURI(String.format(API_PATH_INGEST_FORMAT, templateId.toString()));
    try (
      CloseableHttpResponse response = client.execute(buildGetRequest(uri, ingestTokenValue))
    ) {
      // return content if successful.
      if (!Objects.equals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode()))
        throw buildException(uri.toString(), response);

      String json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      return HubContent.from(jsonProvider.getMapper().readValue(json, HubContentPayload.class));

    } catch (IOException e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public Optional<Template> readPreviewTemplate(UUID userId) throws HubClientException {
    CloseableHttpClient client = httpClientProvider.getClient();
    var path = String.format(API_PATH_USER_TEMPLATE_PLAYBACK_FORMAT, userId);
    var uri = buildURI(path);
    var request = buildGetRequest(uri, ingestTokenValue);
    try (
      CloseableHttpResponse response = client.execute(request)
    ) {
      if (!Objects.equals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode()))
        throw buildException(uri.toString(), response);

      var json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());

      var payload = jsonapiPayloadFactory.deserialize(json);

      return payload.isEmpty() ? Optional.empty() : Optional.of(jsonapiPayloadFactory.toOne(payload));

    } catch (IOException | JsonapiException e) {
      throw new HubClientException(String.format("Failed executing Hub API request to %s", uri), e);
    }
  }

  @Override
  public HubContent load(String shipKey) throws HubClientException {
    var url = String.format("%s%s.json", audioBaseUrl, shipKey);
    LOG.info("Will load to Hub content from {}", url);
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(url))
    ) {
      // return content if successful.
      if (!Objects.equals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode()))
        throw buildException(url, response);

      String json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
      return HubContent.from(jsonProvider.getMapper().readValue(json, HubContentPayload.class));

    } catch (IOException e) {
      throw new HubClientException(e);
    }
  }

  /**
   Set the access token cookie header for a request to Hub

   @param uri              of request
   @param ingestTokenValue of request
   @return http request
   */
  private HttpGet buildGetRequest(URI uri, String ingestTokenValue) {
    var request = new HttpGet(uri);
    request.setHeader(HEADER_COOKIE, String.format("%s=%s", ingestTokenName, ingestTokenValue));
    return request;
  }

  /**
   Build URI for specified API path

   @param path to build URI to
   @return URI for specified API path
   @throws HubClientException on failure to construct URI
   */
  private URI buildURI(String path) throws HubClientException {
    try {
      URIBuilder b = new URIBuilder(String.format("%s%s", ingestUrl, path));
      return b.build();
    } catch (URISyntaxException e) {
      throw new HubClientException("Failed to construct URI", e);
    }
  }

  /**
   Log a failure message and returns a throwable exception based on a response@param uri

   @param response to log and throw
   */
  private HubClientException buildException(String uri, CloseableHttpResponse response) throws HubClientException {
    // if we got here, it's a failure
    LOG.error("Request failed to {}\n response: {} {}", uri, response.getAllHeaders(), response);
    throw new HubClientException(String.format("Request failed to %s\nresponse: %d %s", uri, response.getStatusLine().getStatusCode(), response.getStatusLine().getReasonPhrase()));
  }
}
