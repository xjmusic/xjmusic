// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.hub_client.client;

import com.google.api.client.util.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.hub.ingest.HubContentPayload;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.Environment;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 Implementation of a Hub Client for connecting to Hub and accessing contents
 */
@Singleton
public class HubClientImpl implements HubClient {
  private static final String API_PATH_INGEST_PREFIX = "api/1/ingest/";
  private static final String API_PATH_TEMPLATES_PREFIX = "api/1/templates/";
  private static final String API_PATH_TEMPLATES_PLAYING = "api/1/templates/playing";
  private static final String API_PATH_AUTH = "auth";
  private static final String HEADER_COOKIE = "Cookie";
  private final Logger LOG = LoggerFactory.getLogger(HubClientImpl.class);
  private final CloseableHttpClient httpClient;
  private final String ingestUrl;
  private final String ingestTokenName;
  private final JsonProviderImpl jsonProvider;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final String ingestTokenValue;

  @Inject
  public HubClientImpl(
    Environment env,
    JsonProviderImpl jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory
  ) {
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    httpClient = HttpClients.createDefault();

    ingestUrl = env.getIngestURL();
    ingestTokenName = env.getIngestTokenName();
    ingestTokenValue = env.getIngestTokenValue();

    String obscuredSecret = Arrays.stream(ingestTokenValue.split("")).map(c -> "*").collect(Collectors.joining());
    LOG.info("Will connect to Hub at {} with token '{}' value '{}'", ingestUrl, ingestTokenName, obscuredSecret);
  }

  @Override
  public HubContent ingest(HubClientAccess access, UUID templateId) throws HubClientException {
    try {
      HttpGet request = new HttpGet(buildURI(String.format("%s%s",
        API_PATH_INGEST_PREFIX, templateId.toString())));
      setAccessCookie(request, ingestTokenValue);
      CloseableHttpResponse response = httpClient.execute(request);

      // return content if successful.
      if (Objects.equals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode())) {
        var json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
        HubContentPayload content = jsonProvider.getMapper().readValue(json, HubContentPayload.class);
        List<Object> entities = Lists.newArrayList();
        entities.addAll(content.getTemplates());
        entities.addAll(content.getTemplateBindings());
        entities.addAll(content.getInstruments());
        entities.addAll(content.getInstrumentAudios());
        entities.addAll(content.getInstrumentMemes());
        entities.addAll(content.getPrograms());
        entities.addAll(content.getProgramMemes());
        entities.addAll(content.getProgramSequences());
        entities.addAll(content.getProgramSequenceBindings());
        entities.addAll(content.getProgramSequenceBindingMemes());
        entities.addAll(content.getProgramSequenceChords());
        entities.addAll(content.getProgramSequenceChordVoicings());
        entities.addAll(content.getProgramSequencePatterns());
        entities.addAll(content.getProgramSequencePatternEvents());
        entities.addAll(content.getProgramVoices());
        entities.addAll(content.getProgramVoiceTracks());
        return new HubContent(entities);
      }

      // if we got here, it's a failure
      LOG.error("Failed to request {} because {}", request.getURI(), response);
      throw new HubClientException(String.format("Failed to request %s because %s",
        request.getURI(), response.getStatusLine().getReasonPhrase()));

    } catch (IOException e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public HubClientAccess auth(String accessToken) throws HubClientException {
    HttpGet request = new HttpGet(buildURI(API_PATH_AUTH));
    setAccessCookie(request, accessToken);
    HubClientAccess access;
    CloseableHttpResponse response;
    try {
      response = httpClient.execute(request);
      access = jsonProvider.getMapper().readValue(response.getEntity().getContent(), HubClientAccess.class);
    } catch (IOException e) {
      throw new HubClientException("Failed to authenticate with Hub API", e);
    }
    return access;
  }

  @Override
  public Template readTemplate(String identifier) throws HubClientException {
    try {
      HttpGet request = new HttpGet(buildURI(String.format("%s%s", API_PATH_TEMPLATES_PREFIX, identifier)));
      setAccessCookie(request, ingestTokenValue);
      CloseableHttpResponse response = httpClient.execute(request);

      // return template if found
      if (Objects.equals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode())) {
        var json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
        var payload = jsonapiPayloadFactory.deserialize(json);
        return jsonapiPayloadFactory.toOne(payload);
      }

      throw new HubClientException(String.format("Failed to request %s because %s",
        request.getURI(), response.getStatusLine().getReasonPhrase()));

    } catch (IOException | JsonapiException e) {
      throw new HubClientException(e);
    }
  }

  @Override
  public Collection<Template> readAllTemplatesPlaying() throws HubClientException {
    try {
      HttpGet request = new HttpGet(buildURI(API_PATH_TEMPLATES_PLAYING));
      setAccessCookie(request, ingestTokenValue);
      CloseableHttpResponse response = httpClient.execute(request);

      // return templates if OK
      if (Objects.equals(Response.Status.OK.getStatusCode(), response.getStatusLine().getStatusCode())) {
        var json = IOUtils.toString(response.getEntity().getContent(), Charset.defaultCharset());
        var payload = jsonapiPayloadFactory.deserialize(json);
        return jsonapiPayloadFactory.toMany(payload);
      }

      throw new HubClientException(String.format("Failed to request %s because %s",
        request.getURI(), response.getStatusLine().getReasonPhrase()));

    } catch (IOException | JsonapiException e) {
      throw new HubClientException(e);
    }
  }

  /**
   Set the access token cookie header for a request to Hub

   @param request to set cookie header for
   @param value   to set
   */
  private void setAccessCookie(HttpGet request, String value) {
    request.setHeader(HEADER_COOKIE, String.format("%s=%s", ingestTokenName, value));
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

}
