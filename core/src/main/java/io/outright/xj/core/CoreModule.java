// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core;

import io.outright.xj.core.app.App;
import io.outright.xj.core.app.AppImpl;
import io.outright.xj.core.app.access.AccessControlProvider;
import io.outright.xj.core.app.access.AccessLogFilterProvider;
import io.outright.xj.core.app.access.AccessTokenAuthFilter;
import io.outright.xj.core.app.access.impl.AccessControlProviderImpl;
import io.outright.xj.core.app.access.impl.AccessLogFilterProviderImpl;
import io.outright.xj.core.app.access.impl.AccessTokenAuthFilterImpl;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.app.server.HttpResponseProviderImpl;
import io.outright.xj.core.app.server.HttpServerProvider;
import io.outright.xj.core.app.server.HttpServerProviderImpl;
import io.outright.xj.core.app.server.ResourceConfigProvider;
import io.outright.xj.core.app.server.ResourceConfigProviderImpl;
import io.outright.xj.core.dao.AccountDAO;
import io.outright.xj.core.dao.AccountUserDAO;
import io.outright.xj.core.dao.ArrangementDAO;
import io.outright.xj.core.dao.AudioChordDAO;
import io.outright.xj.core.dao.AudioDAO;
import io.outright.xj.core.dao.AudioEventDAO;
import io.outright.xj.core.dao.AuthDAO;
import io.outright.xj.core.dao.ChainDAO;
import io.outright.xj.core.dao.ChainLibraryDAO;
import io.outright.xj.core.dao.ChoiceDAO;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.dao.IdeaMemeDAO;
import io.outright.xj.core.dao.InstrumentDAO;
import io.outright.xj.core.dao.InstrumentMemeDAO;
import io.outright.xj.core.dao.LibraryDAO;
import io.outright.xj.core.dao.LinkChordDAO;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.dao.MorphDAO;
import io.outright.xj.core.dao.PhaseChordDAO;
import io.outright.xj.core.dao.PhaseDAO;
import io.outright.xj.core.dao.PhaseMemeDAO;
import io.outright.xj.core.dao.PickDAO;
import io.outright.xj.core.dao.PointDAO;
import io.outright.xj.core.dao.UserDAO;
import io.outright.xj.core.dao.VoiceDAO;
import io.outright.xj.core.dao.VoiceEventDAO;
import io.outright.xj.core.dao.impl.AccountDAOImpl;
import io.outright.xj.core.dao.impl.AccountUserDAOImpl;
import io.outright.xj.core.dao.impl.ArrangementDAOImpl;
import io.outright.xj.core.dao.impl.AudioChordDAOImpl;
import io.outright.xj.core.dao.impl.AudioDAOImpl;
import io.outright.xj.core.dao.impl.AudioEventDAOImpl;
import io.outright.xj.core.dao.impl.AuthDAOImpl;
import io.outright.xj.core.dao.impl.ChainDAOImpl;
import io.outright.xj.core.dao.impl.ChainLibraryDAOImpl;
import io.outright.xj.core.dao.impl.ChoiceDAOImpl;
import io.outright.xj.core.dao.impl.IdeaDAOImpl;
import io.outright.xj.core.dao.impl.IdeaMemeDAOImpl;
import io.outright.xj.core.dao.impl.InstrumentDAOImpl;
import io.outright.xj.core.dao.impl.InstrumentMemeDAOImpl;
import io.outright.xj.core.dao.impl.LibraryDAOImpl;
import io.outright.xj.core.dao.impl.LinkChordDAOImpl;
import io.outright.xj.core.dao.impl.LinkDAOImpl;
import io.outright.xj.core.dao.impl.MorphDAOImpl;
import io.outright.xj.core.dao.impl.PhaseChordDAOImpl;
import io.outright.xj.core.dao.impl.PhaseDAOImpl;
import io.outright.xj.core.dao.impl.PhaseMemeDAOImpl;
import io.outright.xj.core.dao.impl.PickDAOImpl;
import io.outright.xj.core.dao.impl.PointDAOImpl;
import io.outright.xj.core.dao.impl.UserDAOImpl;
import io.outright.xj.core.dao.impl.VoiceDAOImpl;
import io.outright.xj.core.dao.impl.VoiceEventDAOImpl;
import io.outright.xj.core.db.RedisDatabaseProvider;
import io.outright.xj.core.db.RedisDatabaseProviderImpl;
import io.outright.xj.core.db.sql.SQLDatabaseProvider;
import io.outright.xj.core.db.sql.SQLDatabaseProviderImpl;
import io.outright.xj.core.external.amazon.AmazonProvider;
import io.outright.xj.core.external.amazon.AmazonProviderImpl;
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
    bindApp();
    bindDAO();
    bindExternal();
    bindUtil();
  }

  private void bindApp() {
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

  private void bindDAO() {
    bind(AccountDAO.class).to(AccountDAOImpl.class);
    bind(AccountUserDAO.class).to(AccountUserDAOImpl.class);
    bind(ArrangementDAO.class).to(ArrangementDAOImpl.class);
    bind(AudioChordDAO.class).to(AudioChordDAOImpl.class);
    bind(AudioDAO.class).to(AudioDAOImpl.class);
    bind(AudioEventDAO.class).to(AudioEventDAOImpl.class);
    bind(AuthDAO.class).to(AuthDAOImpl.class);
    bind(ChainDAO.class).to(ChainDAOImpl.class);
    bind(ChainLibraryDAO.class).to(ChainLibraryDAOImpl.class);
    bind(ChoiceDAO.class).to(ChoiceDAOImpl.class);
    bind(IdeaDAO.class).to(IdeaDAOImpl.class);
    bind(IdeaMemeDAO.class).to(IdeaMemeDAOImpl.class);
    bind(InstrumentDAO.class).to(InstrumentDAOImpl.class);
    bind(InstrumentMemeDAO.class).to(InstrumentMemeDAOImpl.class);
    bind(LibraryDAO.class).to(LibraryDAOImpl.class);
    bind(LinkChordDAO.class).to(LinkChordDAOImpl.class);
    bind(LinkDAO.class).to(LinkDAOImpl.class);
    bind(MorphDAO.class).to(MorphDAOImpl.class);
    bind(PhaseChordDAO.class).to(PhaseChordDAOImpl.class);
    bind(PhaseDAO.class).to(PhaseDAOImpl.class);
    bind(PhaseMemeDAO.class).to(PhaseMemeDAOImpl.class);
    bind(PickDAO.class).to(PickDAOImpl.class);
    bind(PointDAO.class).to(PointDAOImpl.class);
    bind(UserDAO.class).to(UserDAOImpl.class);
    bind(VoiceDAO.class).to(VoiceDAOImpl.class);
    bind(VoiceEventDAO.class).to(VoiceEventDAOImpl.class);
  }

  private void bindExternal() {
    bind(GoogleHttpProvider.class).to(GoogleHttpProviderImpl.class);
    bind(GoogleProvider.class).to(GoogleProviderImpl.class);
    bind(AmazonProvider.class).to(AmazonProviderImpl.class);
  }

  private void bindUtil() {
    bind(TokenGenerator.class).to(TokenGeneratorImpl.class);
  }

}
