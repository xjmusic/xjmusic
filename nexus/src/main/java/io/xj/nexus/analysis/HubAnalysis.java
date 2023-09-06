// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.analysis;

import io.xj.nexus.hub_client.access.HubAccess;

/**
 Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
public interface HubAnalysis {

  /**
   Get the access with which this HubAnalysis was instantiated.

   @return access
   */
  HubAccess getAccess();

  /**
   Express all hub content as HTML.

   @return HTML of all hub content
   @throws HubAnalysisException on failure
   */
  String toHTML() throws HubAnalysisException;

  /**
   Get a string representation of analysis
   */
  String toString();

}
