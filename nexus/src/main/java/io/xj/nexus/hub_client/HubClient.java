// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.hub_client;

import io.xj.hub.HubContent;

import java.util.UUID;

/**
 Interface of a Hub Client for connecting to Hub and accessing contents
 */
public interface HubClient {

  /**
   Push content to the API v2 (HubContent)
   <p>
   Make a remote call to Hub, and POST the specified entities
   <p>
   HubClient allows a service that depends on Hub (e.g. Nexus) to connect to the Hub REST API via an HTTP client and deserialize results into usable entities
   <p>
   HubAccess entity contains the token itself, such that one of these entities can also be used (e.g. by a HubClient) in order to make a request to a Hub API
   <p>
   Workstation has Project → Push feature to publish the on-disk version of the project to the Lab (overwriting the Lab version)
   https://www.pivotaltracker.com/story/show/187004700

   @param baseUrl of Hub
   @param access  control
   @param content to post
   @throws HubClientException on failure to perform request
   */
  void postProjectSyncApiV2(String baseUrl, HubClientAccess access, HubContent content) throws HubClientException;

  /**
   Ingest content from the API v2 (HubContent)
   <p>
   Make a remote call to Hub, and ingest the specified entities
   <p>
   HubClient allows a service that depends on Hub (e.g. Nexus) to connect to the Hub REST API via an HTTP client and deserialize results into usable entities
   <p>
   HubAccess entity contains the token itself, such that one of these entities can also be used (e.g. by a HubClient) in order to make a request to a Hub API

   @param baseUrl   of Hub
   @param access    control
   @param projectId to ingest
   @return HubClient comprising ingested entities, including all child sub-entities
   @throws HubClientException on failure to perform request
   */
  HubContent getProjectApiV2(String baseUrl, HubClientAccess access, UUID projectId) throws HubClientException;

  /**
   Load shipped content from a static file in API v1 (HubContentPayload a.k.a. JSONAPI)
   <p>
   Nexus production fabrication from static source (without Hub) https://www.pivotaltracker.com/story/show/177020318

   @param shipKey      to load
   @param audioBaseUrl to use for audio
   @return hub content
   */
  HubContent loadApiV1(String shipKey, String audioBaseUrl) throws HubClientException;
}
