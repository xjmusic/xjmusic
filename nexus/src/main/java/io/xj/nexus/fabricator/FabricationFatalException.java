// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.fabricator;

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
}
