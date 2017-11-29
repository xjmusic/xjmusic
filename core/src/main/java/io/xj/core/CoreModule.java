// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core;

import io.xj.core.app.App;
import io.xj.core.app.AppImpl;
import io.xj.core.app.access.AccessControlProvider;
import io.xj.core.app.access.AccessLogFilterProvider;
import io.xj.core.app.access.AccessTokenAuthFilter;
import io.xj.core.app.access.impl.AccessControlProviderImpl;
import io.xj.core.app.access.impl.AccessLogFilterProviderImpl;
import io.xj.core.app.access.impl.AccessTokenAuthFilterImpl;
import io.xj.core.app.server.HttpResponseProvider;
import io.xj.core.app.server.HttpResponseProviderImpl;
import io.xj.core.app.server.HttpServerProvider;
import io.xj.core.app.server.HttpServerProviderImpl;
import io.xj.core.app.server.ResourceConfigProvider;
import io.xj.core.app.server.ResourceConfigProviderImpl;
import io.xj.core.app.stats.StatsProvider;
import io.xj.core.app.stats.StatsProviderImpl;
import io.xj.core.cache.audio.AudioCacheProvider;
import io.xj.core.cache.audio.impl.AudioCacheProviderImpl;
import io.xj.core.dao.AccountDAO;
import io.xj.core.dao.AccountUserDAO;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.dao.AuthDAO;
import io.xj.core.dao.ChainConfigDAO;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.ChainIdeaDAO;
import io.xj.core.dao.ChainInstrumentDAO;
import io.xj.core.dao.ChainLibraryDAO;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.IdeaDAO;
import io.xj.core.dao.IdeaMemeDAO;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.dao.LinkChordDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMemeDAO;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.dao.PhaseChordDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.dao.PhaseMemeDAO;
import io.xj.core.dao.PickDAO;
import io.xj.core.dao.UserDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.dao.VoiceEventDAO;
import io.xj.core.dao.impl.AccountDAOImpl;
import io.xj.core.dao.impl.AccountUserDAOImpl;
import io.xj.core.dao.impl.ArrangementDAOImpl;
import io.xj.core.dao.impl.AudioChordDAOImpl;
import io.xj.core.dao.impl.AudioDAOImpl;
import io.xj.core.dao.impl.AudioEventDAOImpl;
import io.xj.core.dao.impl.AuthDAOImpl;
import io.xj.core.dao.impl.ChainConfigDAOImpl;
import io.xj.core.dao.impl.ChainDAOImpl;
import io.xj.core.dao.impl.ChainIdeaDAOImpl;
import io.xj.core.dao.impl.ChainInstrumentDAOImpl;
import io.xj.core.dao.impl.ChainLibraryDAOImpl;
import io.xj.core.dao.impl.ChoiceDAOImpl;
import io.xj.core.dao.impl.IdeaDAOImpl;
import io.xj.core.dao.impl.IdeaMemeDAOImpl;
import io.xj.core.dao.impl.InstrumentDAOImpl;
import io.xj.core.dao.impl.InstrumentMemeDAOImpl;
import io.xj.core.dao.impl.LibraryDAOImpl;
import io.xj.core.dao.impl.LinkChordDAOImpl;
import io.xj.core.dao.impl.LinkDAOImpl;
import io.xj.core.dao.impl.LinkMemeDAOImpl;
import io.xj.core.dao.impl.LinkMessageDAOImpl;
import io.xj.core.dao.impl.PhaseChordDAOImpl;
import io.xj.core.dao.impl.PhaseDAOImpl;
import io.xj.core.dao.impl.PhaseMemeDAOImpl;
import io.xj.core.dao.impl.PickDAOImpl;
import io.xj.core.dao.impl.UserDAOImpl;
import io.xj.core.dao.impl.VoiceDAOImpl;
import io.xj.core.dao.impl.VoiceEventDAOImpl;
import io.xj.core.database.redis.RedisDatabaseProvider;
import io.xj.core.database.redis.impl.RedisDatabaseProviderImpl;
import io.xj.core.database.sql.SQLDatabaseProvider;
import io.xj.core.database.sql.impl.SQLDatabaseProviderImpl;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.external.amazon.AmazonProviderImpl;
import io.xj.core.external.google.GoogleHttpProvider;
import io.xj.core.external.google.GoogleHttpProviderImpl;
import io.xj.core.external.google.GoogleProvider;
import io.xj.core.external.google.GoogleProviderImpl;
import io.xj.core.util.token.TokenGenerator;
import io.xj.core.util.token.TokenGeneratorImpl;
import io.xj.core.work.WorkManager;
import io.xj.core.work.basis.Basis;
import io.xj.core.work.basis.BasisFactory;
import io.xj.core.work.basis.impl.BasisImpl;
import io.xj.core.work.impl.WorkManagerImpl;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class CoreModule extends AbstractModule {
  protected void configure() {
    bindApp();
    bindDAO();
    bindExternal();
    installBasisFactory();
  }

  private void bindApp() {
    bind(AccessControlProvider.class).to(AccessControlProviderImpl.class);
    bind(AccessLogFilterProvider.class).to(AccessLogFilterProviderImpl.class);
    bind(AccessTokenAuthFilter.class).to(AccessTokenAuthFilterImpl.class);
    bind(App.class).to(AppImpl.class);
    bind(AudioCacheProvider.class).to(AudioCacheProviderImpl.class);
    bind(DataStoreFactory.class).to(MemoryDataStoreFactory.class);
    bind(HttpResponseProvider.class).to(HttpResponseProviderImpl.class);
    bind(HttpServerProvider.class).to(HttpServerProviderImpl.class);
    bind(HttpTransport.class).to(NetHttpTransport.class);
    bind(JsonFactory.class).to(JacksonFactory.class);
    bind(JsonFactory.class).to(JacksonFactory.class);
    bind(RedisDatabaseProvider.class).to(RedisDatabaseProviderImpl.class);
    bind(ResourceConfigProvider.class).to(ResourceConfigProviderImpl.class);
    bind(SQLDatabaseProvider.class).to(SQLDatabaseProviderImpl.class);
    bind(StatsProvider.class).to(StatsProviderImpl.class);
    bind(TokenGenerator.class).to(TokenGeneratorImpl.class);
    bind(WorkManager.class).to(WorkManagerImpl.class);
  }

  private void installBasisFactory() {
    install(new FactoryModuleBuilder()
      .implement(Basis.class, BasisImpl.class)
      .build(BasisFactory.class));
  }

  private void bindDAO() {
    bind(AccountDAO.class).to(AccountDAOImpl.class);
    bind(AccountUserDAO.class).to(AccountUserDAOImpl.class);
    bind(ArrangementDAO.class).to(ArrangementDAOImpl.class);
    bind(AudioChordDAO.class).to(AudioChordDAOImpl.class);
    bind(AudioDAO.class).to(AudioDAOImpl.class);
    bind(AudioEventDAO.class).to(AudioEventDAOImpl.class);
    bind(AuthDAO.class).to(AuthDAOImpl.class);
    bind(ChainConfigDAO.class).to(ChainConfigDAOImpl.class);
    bind(ChainDAO.class).to(ChainDAOImpl.class);
    bind(ChainIdeaDAO.class).to(ChainIdeaDAOImpl.class);
    bind(ChainInstrumentDAO.class).to(ChainInstrumentDAOImpl.class);
    bind(ChainLibraryDAO.class).to(ChainLibraryDAOImpl.class);
    bind(ChoiceDAO.class).to(ChoiceDAOImpl.class);
    bind(IdeaDAO.class).to(IdeaDAOImpl.class);
    bind(IdeaMemeDAO.class).to(IdeaMemeDAOImpl.class);
    bind(InstrumentDAO.class).to(InstrumentDAOImpl.class);
    bind(InstrumentMemeDAO.class).to(InstrumentMemeDAOImpl.class);
    bind(LibraryDAO.class).to(LibraryDAOImpl.class);
    bind(LinkChordDAO.class).to(LinkChordDAOImpl.class);
    bind(LinkDAO.class).to(LinkDAOImpl.class);
    bind(LinkMemeDAO.class).to(LinkMemeDAOImpl.class);
    bind(LinkMessageDAO.class).to(LinkMessageDAOImpl.class);
    bind(PhaseChordDAO.class).to(PhaseChordDAOImpl.class);
    bind(PhaseDAO.class).to(PhaseDAOImpl.class);
    bind(PhaseMemeDAO.class).to(PhaseMemeDAOImpl.class);
    bind(PickDAO.class).to(PickDAOImpl.class);
    bind(UserDAO.class).to(UserDAOImpl.class);
    bind(VoiceDAO.class).to(VoiceDAOImpl.class);
    bind(VoiceEventDAO.class).to(VoiceEventDAOImpl.class);
  }

  private void bindExternal() {
    bind(AmazonProvider.class).to(AmazonProviderImpl.class);
    bind(GoogleHttpProvider.class).to(GoogleHttpProviderImpl.class);
    bind(GoogleProvider.class).to(GoogleProviderImpl.class);
  }

}
