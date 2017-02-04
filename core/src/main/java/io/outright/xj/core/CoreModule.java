// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core;

import io.outright.xj.core.app.App;
import io.outright.xj.core.app.AppImpl;
import io.outright.xj.core.app.access.AccessControlProvider;
import io.outright.xj.core.app.access.AccessControlProviderImpl;
import io.outright.xj.core.app.access.AccessLogFilterProvider;
import io.outright.xj.core.app.access.AccessLogFilterProviderImpl;
import io.outright.xj.core.app.access.AccessTokenAuthFilter;
import io.outright.xj.core.app.access.AccessTokenAuthFilterImpl;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.app.server.HttpResponseProviderImpl;
import io.outright.xj.core.app.server.HttpServerProvider;
import io.outright.xj.core.app.server.HttpServerProviderImpl;
import io.outright.xj.core.app.server.ResourceConfigProvider;
import io.outright.xj.core.app.server.ResourceConfigProviderImpl;
import io.outright.xj.core.dao.AccountDAO;
import io.outright.xj.core.dao.AccountUserDAO;
import io.outright.xj.core.dao.AuthDAO;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.dao.IdeaMemeDAO;
import io.outright.xj.core.dao.LibraryDAO;
import io.outright.xj.core.dao.PhaseChordDAO;
import io.outright.xj.core.dao.PhaseDAO;
import io.outright.xj.core.dao.PhaseMemeDAO;
import io.outright.xj.core.dao.UserDAO;
import io.outright.xj.core.dao.VoiceDAO;
import io.outright.xj.core.dao.VoiceEventDAO;
import io.outright.xj.core.dao.impl.AccountDAOImpl;
import io.outright.xj.core.dao.impl.AccountUserDAOImpl;
import io.outright.xj.core.dao.impl.AuthDAOImpl;
import io.outright.xj.core.dao.impl.IdeaDAOImpl;
import io.outright.xj.core.dao.impl.IdeaMemeDAOImpl;
import io.outright.xj.core.dao.impl.LibraryDAOImpl;
import io.outright.xj.core.dao.impl.PhaseChordDAOImpl;
import io.outright.xj.core.dao.impl.PhaseDAOImpl;
import io.outright.xj.core.dao.impl.PhaseMemeDAOImpl;
import io.outright.xj.core.dao.impl.UserDAOImpl;
import io.outright.xj.core.dao.impl.VoiceDAOImpl;
import io.outright.xj.core.dao.impl.VoiceEventDAOImpl;
import io.outright.xj.core.db.RedisDatabaseProvider;
import io.outright.xj.core.db.RedisDatabaseProviderImpl;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.db.sql.SQLDatabaseProviderImpl;
import io.outright.xj.core.external.google.GoogleHttpProvider;
import io.outright.xj.core.external.google.GoogleHttpProviderImpl;
import io.outright.xj.core.external.google.GoogleProvider;
import io.outright.xj.core.external.google.GoogleProviderImpl;
import io.outright.xj.core.util.token.TokenGenerator;
import io.outright.xj.core.util.token.TokenGeneratorImpl;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.inject.AbstractModule;

public class CoreModule extends AbstractModule {
  protected void configure() {
    configureApp();
    configureUtil();
    configureDAO();
    configureExternal();
  }

  private void configureApp() {
    bind(AccessControlProvider.class).to(AccessControlProviderImpl.class);
    bind(AccessLogFilterProvider.class).to(AccessLogFilterProviderImpl.class);
    bind(AccessTokenAuthFilter.class).to(AccessTokenAuthFilterImpl.class);
    bind(App.class).to(AppImpl.class);
    bind(DataStoreFactory.class).to(MemoryDataStoreFactory.class);
    bind(HttpResponseProvider.class).to(HttpResponseProviderImpl.class);
    bind(HttpServerProvider.class).to(HttpServerProviderImpl.class);
    bind(HttpTransport.class).to(NetHttpTransport.class);
    bind(JsonFactory.class).to(JacksonFactory.class);
    bind(JsonFactory.class).to(JacksonFactory.class);
    bind(RedisDatabaseProvider.class).to(RedisDatabaseProviderImpl.class);
    bind(ResourceConfigProvider.class).to(ResourceConfigProviderImpl.class);
    bind(SQLDatabaseProvider.class).to(SQLDatabaseProviderImpl.class);
  }

  private void configureUtil() {
    bind(TokenGenerator.class).to(TokenGeneratorImpl.class);
  }

  private void configureExternal() {
    bind(GoogleHttpProvider.class).to(GoogleHttpProviderImpl.class);
    bind(GoogleProvider.class).to(GoogleProviderImpl.class);
  }

  private void configureDAO() {
    bind(AccountDAO.class).to(AccountDAOImpl.class);
    bind(AccountUserDAO.class).to(AccountUserDAOImpl.class);
    bind(AuthDAO.class).to(AuthDAOImpl.class);
    bind(IdeaDAO.class).to(IdeaDAOImpl.class);
    bind(IdeaMemeDAO.class).to(IdeaMemeDAOImpl.class);
    bind(LibraryDAO.class).to(LibraryDAOImpl.class);
    bind(PhaseDAO.class).to(PhaseDAOImpl.class);
    bind(PhaseChordDAO.class).to(PhaseChordDAOImpl.class);
    bind(PhaseMemeDAO.class).to(PhaseMemeDAOImpl.class);
    bind(UserDAO.class).to(UserDAOImpl.class);
    bind(VoiceDAO.class).to(VoiceDAOImpl.class);
    bind(VoiceEventDAO.class).to(VoiceEventDAOImpl.class);
  }
}
