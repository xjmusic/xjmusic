// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.hub_client.access;

/**
 Generates access tokens
 */
public interface HubAccessTokenGenerator {
  /**
   Generate a new access token

   @return new access token
   */
  String generate();
}
