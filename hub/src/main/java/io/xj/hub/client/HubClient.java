// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.client;

import io.xj.hub.tables.pojos.Template;

import java.util.Optional;
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

   @param access     control
   @param templateId to ingest
   @return HubClient comprising ingested entities, including all child sub-entities
   @throws HubClientException on failure to perform request
   */
  HubContent ingest(HubClientAccess access, UUID templateId) throws HubClientException;

  /**
   Read all Templates playing from Hub

   @return Templates currently playing
   @param userId for which to read to template
   */
  Optional<Template> readPreviewTemplate(UUID userId) throws HubClientException;

  /**
   Load shipped content from a static file
   <p>
   Nexus production fabrication from static source (without Hub) https://www.pivotaltracker.com/story/show/177020318

   @param shipKey to load
   @return hub content
   */
  HubContent load(String shipKey) throws HubClientException;
}
