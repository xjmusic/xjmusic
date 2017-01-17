// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import io.outright.xj.core.app.App;
import io.outright.xj.core.app.AppImpl;
import io.outright.xj.core.app.access.AccessControlModuleProvider;
import io.outright.xj.core.app.access.AccessControlModuleProviderImpl;
import io.outright.xj.core.app.access.AccessLogFilterProvider;
import io.outright.xj.core.app.access.AccessLogFilterProviderImpl;
import io.outright.xj.core.app.access.AccessTokenAuthFilter;
import io.outright.xj.core.app.access.AccessTokenAuthFilterImpl;
import io.outright.xj.core.app.db.RedisDatabaseProvider;
import io.outright.xj.core.app.db.RedisDatabaseProviderImpl;
import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.db.SQLDatabaseProviderImpl;
import io.outright.xj.core.app.output.JSONOutputProvider;
import io.outright.xj.core.app.output.JSONOutputProviderImpl;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.app.server.HttpResponseProviderImpl;
import io.outright.xj.core.app.server.HttpServerProvider;
import io.outright.xj.core.app.server.HttpServerProviderImpl;
import io.outright.xj.core.app.server.ResourceConfigProvider;
import io.outright.xj.core.app.server.ResourceConfigProviderImpl;
import io.outright.xj.core.external.google.GoogleHttpProvider;
import io.outright.xj.core.external.google.GoogleHttpProviderImpl;
import io.outright.xj.core.external.google.GoogleProvider;
import io.outright.xj.core.external.google.GoogleProviderImpl;
import io.outright.xj.core.util.token.TokenGenerator;
import io.outright.xj.core.util.token.TokenGeneratorImpl;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.inject.AbstractModule;

public class CoreModule extends AbstractModule {
  protected void configure() {
    bind(AccessControlModuleProvider.class).to(AccessControlModuleProviderImpl.class);
    bind(AccessLogFilterProvider.class).to(AccessLogFilterProviderImpl.class);
    bind(AccessTokenAuthFilter.class).to(AccessTokenAuthFilterImpl.class);
    bind(App.class).to(AppImpl.class);
    bind(DataStoreFactory.class).to(MemoryDataStoreFactory.class);
    bind(GoogleHttpProvider.class).to(GoogleHttpProviderImpl.class);
    bind(GoogleProvider.class).to(GoogleProviderImpl.class);
    bind(HttpResponseProvider.class).to(HttpResponseProviderImpl.class);
    bind(HttpServerProvider.class).to(HttpServerProviderImpl.class);
    bind(HttpTransport.class).to(NetHttpTransport.class);
    bind(JsonFactory.class).to(JacksonFactory.class);
    bind(JsonFactory.class).to(JacksonFactory.class);
    bind(JSONOutputProvider.class).to(JSONOutputProviderImpl.class);
    bind(RedisDatabaseProvider.class).to(RedisDatabaseProviderImpl.class);
    bind(ResourceConfigProvider.class).to(ResourceConfigProviderImpl.class);
    bind(SQLDatabaseProvider.class).to(SQLDatabaseProviderImpl.class);
    bind(TokenGenerator.class).to(TokenGeneratorImpl.class);
  }
}
