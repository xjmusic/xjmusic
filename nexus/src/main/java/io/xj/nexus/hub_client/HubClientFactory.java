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
   * Push content to the API v2 (HubContent)
   * <p>
   * Make a remote call to Hub, and POST the specified entities
   * <p>
   * HubClientFactory allows a service that depends on Hub (e.g. Nexus) to connect to the Hub REST API via an HTTP client and deserialize results into usable entities
   * <p>
   * HubAccess entity contains the token itself, such that one of these entities can also be used (e.g. by a HubClientFactory) in order to make a request to a Hub API
   * <p>
   * Workstation has Project → Push feature to publish the on-disk version of the project to the Lab (overwriting the Lab version)
   * https://www.pivotaltracker.com/story/show/187004700
   *
   * @param httpClient to use
   * @param baseUrl    of Hub
   * @param access     control
   * @param content    to post
   * @throws HubClientException on failure to perform request
   */
  void postProjectSyncApiV2(CloseableHttpClient httpClient, String baseUrl, HubClientAccess access, HubContent content) throws HubClientException;

  /**
   * Ingest content from the API v2 (HubContent)
   * <p>
   * Make a remote call to Hub, and ingest the specified entities
   * <p>
   * HubClientFactory allows a service that depends on Hub (e.g. Nexus) to connect to the Hub REST API via an HTTP client and deserialize results into usable entities
   * <p>
   * HubAccess entity contains the token itself, such that one of these entities can also be used (e.g. by a HubClientFactory) in order to make a request to a Hub API
   *
   * @param httpClient to use
   * @param baseUrl    of Hub
   * @param access     control
   * @param projectId  to ingest
   * @return HubClientFactory comprising ingested entities, including all child sub-entities
   * @throws HubClientException on failure to perform request
   */
  HubContent getProjectApiV2(CloseableHttpClient httpClient, String baseUrl, HubClientAccess access, UUID projectId) throws HubClientException;

  /**
   * Authorize an instrument audio upload from the API v2 (HubUploadAuthorization)
   * <p>
   * Workstation has Project → Push feature to publish the on-disk version of the project to the Lab (overwriting the Lab version)
   * https://www.pivotaltracker.com/story/show/187004700
   *
   * @param httpClient        to use
   * @param baseUrl           of Hub
   * @param access            control
   * @param instrumentAudioId of the audio which to authorize upload
   * @param extension         of the audio file
   * @return HubUploadAuthorization
   * @throws HubClientException on failure to perform request
   */
  HubUploadAuthorization authorizeInstrumentAudioUploadApiV2(CloseableHttpClient httpClient, String baseUrl, HubClientAccess access, UUID instrumentAudioId, String extension) throws HubClientException;

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
   * Upload a file from the given URL to the given output path
   *
   * @param hubAccess  control
   * @param hubBaseUrl base URL of lab
   * @param httpClient http client (don't close the client; only close the responses from it)
   * @param upload     audio upload request/result object
   */
  void uploadInstrumentAudioFile(HubClientAccess hubAccess, String hubBaseUrl, CloseableHttpClient httpClient, ProjectAudioUpload upload);

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
