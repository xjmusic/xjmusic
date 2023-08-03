// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.analysis;

import io.xj.lib.util.StringUtils;

/**
 * Template content Analysis https://www.pivotaltracker.com/story/show/161199945
 */
public class HubAnalysisException extends Exception {

  public HubAnalysisException(String msg) {
    super(msg);
  }

  public HubAnalysisException(String msg, Exception e) {
    super(String.format("%s %s\n%s", msg, e.toString(), StringUtils.formatStackTrace(e)));
  }

  public HubAnalysisException(Throwable targetException) {
    super(String.format("%s\n%s", targetException.getMessage(), StringUtils.formatStackTrace(targetException)));
  }
}
