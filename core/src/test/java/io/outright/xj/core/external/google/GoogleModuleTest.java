// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.external.google;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;

public class GoogleModuleTest {
  private static final Injector injector = Guice.createInjector(new GoogleModule());
  @Test
  public void configure() throws Exception {
    assert injector.getInstance(DataStoreFactory.class) != null;
    assert injector.getInstance(GoogleProvider.class) != null;
    assert injector.getInstance(HttpTransport.class) != null;
    assert injector.getInstance(JsonFactory.class) != null;
  }
}
