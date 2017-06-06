// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.external.google;

import com.google.api.client.http.HttpTransport;

public interface GoogleHttpProvider {
  HttpTransport getTransport();
}
