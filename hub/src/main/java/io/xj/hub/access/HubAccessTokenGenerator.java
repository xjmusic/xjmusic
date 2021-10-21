// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

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
