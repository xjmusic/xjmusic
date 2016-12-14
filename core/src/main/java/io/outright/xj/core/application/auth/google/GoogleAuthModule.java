// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.auth.google;

import io.outright.xj.core.application.server.BaseUrlProvider;
import io.outright.xj.core.application.server.BaseUrlProviderImpl;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.inject.AbstractModule;

public class GoogleAuthModule extends AbstractModule {
  protected void configure() {
    bind(BaseUrlProvider.class).to(BaseUrlProviderImpl.class);
    bind(DataStoreFactory.class).to(MemoryDataStoreFactory.class);
    bind(GoogleAuthProvider.class).to(GoogleAuthProviderImpl.class);
    bind(GoogleOAuth2Credentials.class).to(GoogleOAuth2CredentialsImpl.class);
    bind(HttpTransport.class).to(NetHttpTransport.class);
    bind(JsonFactory.class).to(JacksonFactory.class);
  }
}
