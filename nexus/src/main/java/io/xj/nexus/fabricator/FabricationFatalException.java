// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.base.Strings;

import javax.annotation.Nullable;

/**
 When this occurs during fabrication, the chain must be restarted.
 This differentiates from retry-able network or service failures.
 <p>
 Fabrication should recover from having no main choice https://www.pivotaltracker.com/story/show/182131722
 */
public class FabricationFatalException extends Exception {

  public FabricationFatalException(String msg) {
    super(msg);
  }

  public FabricationFatalException(@Nullable String msg, Exception e) {
    super(String.format("%s%s\n%s", Strings.isNullOrEmpty(msg) ? "" : msg + " ", e.getMessage(), e));
    setStackTrace(e.getStackTrace());
  }

  public FabricationFatalException(Throwable e) {
    super(null, e);
  }

  public FabricationFatalException() {
    super();
  }
}
