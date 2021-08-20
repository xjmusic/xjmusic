// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.hub_client.client;

import java.util.UUID;

/**
 Interface of a Hub Client for connecting to Hub and accessing contents
 */
public interface HubClient {

  /**
   Make a remote call to Hub, and ingest the specified entities
   <p>
   HubClient allows a service that depends on Hub (e.g. Nexus) to connect to the Hub REST API via an HTTP client and deserialize results into usable entities
   <p>
   HubAccess entity contains the token itself, such that one of these entities can also be used (e.g. by a HubClient) in order to make a request to a Hub API

   @return HubClient comprising ingested entities, including all child sub-entities
   @throws HubClientException on failure to perform request
   @param access        control
   @param templateId    to ingest
   */
  HubContent ingest(HubClientAccess access, UUID templateId) throws HubClientException;

  /**
   Retrieve a HubClientAccess entity from Hub via the /auth endpoint
   <p>
   Nexus service is able to retrieve HubAccess from its request context, by requesting access for a specified cookie from the Hub service

   @param accessToken to get HubClientAccess for
   @return HubClientAccess for given auth token
   @throws HubClientException on failure to authenticate
   */
  HubClientAccess auth(String accessToken) throws HubClientException;
}
