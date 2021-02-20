// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.client;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Objects;

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
