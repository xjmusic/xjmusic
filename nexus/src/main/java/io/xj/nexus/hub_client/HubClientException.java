// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.hub_client;

public class HubClientException extends Exception {

  public HubClientException(String msg) {
    super(msg);
  }

  public HubClientException(String msg, Exception e) {
    super(msg, e);
  }

  public HubClientException(Throwable targetException) {
    super(targetException);
  }
}
