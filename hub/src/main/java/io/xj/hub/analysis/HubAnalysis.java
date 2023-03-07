// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.analysis;

import io.xj.hub.access.HubAccess;

/**
 * Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
public interface HubAnalysis {

  /**
   * Get the access with which this HubAnalysis was instantiated.
   *
   * @return access
   */
  HubAccess getAccess();

  /**
   * Express all hub content as HTML.
   *
   * @return HTML of all hub content
   * @throws HubAnalysisException on failure
   */
  String toHTML() throws HubAnalysisException;

  /**
   * Get a string representation of analysis
   */
  String toString();

}
