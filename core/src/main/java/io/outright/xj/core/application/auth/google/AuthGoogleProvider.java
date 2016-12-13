// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.auth.google;

import java.io.IOException;

public interface AuthGoogleProvider {
  void setup(String clientId, String clientSecret) throws IOException;
}
