// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.external.google;

import com.google.api.client.http.HttpTransport;

public interface GoogleHttpProvider {
  HttpTransport getTransport();
}
