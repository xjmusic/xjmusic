// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.core.access.AccessControlProvider;
import io.xj.core.access.AccessLogFilterProvider;
import io.xj.core.access.AccessTokenAuthFilter;
import io.xj.core.access.impl.AccessControlProviderImpl;
import io.xj.core.access.impl.AccessLogFilterProviderImpl;
import io.xj.core.access.impl.AccessTokenAuthFilterImpl;
import io.xj.core.access.token.TokenGenerator;
import io.xj.core.access.token.TokenGeneratorImpl;
import io.xj.core.app.App;
import io.xj.core.app.Health;
import io.xj.core.app.Heartbeat;
import io.xj.core.app.impl.AppImpl;
import io.xj.core.app.impl.HealthImpl;
import io.xj.core.app.impl.HeartbeatImpl;
import io.xj.core.cache.audio.AudioCacheProvider;
import io.xj.core.cache.audio.impl.AudioCacheProviderImpl;
import io.xj.core.dao.AccountDAO;
import io.xj.core.dao.AccountUserDAO;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.dao.ChainConfigDAO;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.ChainInstrumentDAO;
import io.xj.core.dao.ChainLibraryDAO;
import io.xj.core.dao.ChainSequenceDAO;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.dao.PatternChordDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternEventDAO;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.dao.SequenceDAO;
import io.xj.core.dao.SequenceMemeDAO;
import io.xj.core.dao.SequencePatternDAO;
import io.xj.core.dao.SequencePatternMemeDAO;
import io.xj.core.dao.UserDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.dao.impl.AccountDAOImpl;
import io.xj.core.dao.impl.AccountUserDAOImpl;
import io.xj.core.dao.impl.AudioChordDAOImpl;
import io.xj.core.dao.impl.AudioDAOImpl;
import io.xj.core.dao.impl.AudioEventDAOImpl;
import io.xj.core.dao.impl.ChainConfigDAOImpl;
import io.xj.core.dao.impl.ChainDAOImpl;
import io.xj.core.dao.impl.ChainInstrumentDAOImpl;
import io.xj.core.dao.impl.ChainLibraryDAOImpl;
import io.xj.core.dao.impl.ChainSequenceDAOImpl;
import io.xj.core.dao.impl.InstrumentDAOImpl;
import io.xj.core.dao.impl.InstrumentMemeDAOImpl;
import io.xj.core.dao.impl.LibraryDAOImpl;
import io.xj.core.dao.impl.PatternChordDAOImpl;
import io.xj.core.dao.impl.PatternDAOImpl;
import io.xj.core.dao.impl.PatternEventDAOImpl;
import io.xj.core.dao.impl.PlatformMessageDAOImpl;
import io.xj.core.dao.impl.SegmentDAOImpl;
import io.xj.core.dao.impl.SequenceDAOImpl;
import io.xj.core.dao.impl.SequenceMemeDAOImpl;
import io.xj.core.dao.impl.SequencePatternDAOImpl;
import io.xj.core.dao.impl.SequencePatternMemeDAOImpl;
import io.xj.core.dao.impl.UserDAOImpl;
import io.xj.core.dao.impl.VoiceDAOImpl;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.external.amazon.AmazonProviderImpl;
import io.xj.core.external.google.GoogleHttpProvider;
import io.xj.core.external.google.GoogleHttpProviderImpl;
import io.xj.core.external.google.GoogleProvider;
import io.xj.core.external.google.GoogleProviderImpl;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.fabricator.impl.FabricatorImpl;
import io.xj.core.ingest.Ingest;
import io.xj.core.ingest.IngestFactory;
import io.xj.core.ingest.cache.IngestCacheProvider;
import io.xj.core.ingest.cache.impl.IngestCacheProviderImpl;
import io.xj.core.ingest.impl.IngestImpl;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.impl.SegmentImpl;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import io.xj.core.persistence.redis.impl.RedisDatabaseProviderImpl;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.impl.SQLDatabaseProviderImpl;
import io.xj.core.transport.GsonProvider;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.core.transport.HttpServerProvider;
import io.xj.core.transport.ResourceConfigProvider;
import io.xj.core.transport.StatsProvider;
import io.xj.core.transport.impl.GsonProviderImpl;
import io.xj.core.transport.impl.HttpResponseProviderImpl;
import io.xj.core.transport.impl.HttpServerProviderImpl;
import io.xj.core.transport.impl.ResourceConfigProviderImpl;
import io.xj.core.transport.impl.StatsProviderImpl;
import io.xj.core.work.WorkManager;
import io.xj.core.work.impl.WorkManagerImpl;
import io.xj.mixer.MixerModule;

public class CoreModule extends AbstractModule {
  @Override
  protected void configure() {
    bindApp();
    bindDAO();
    bindExternal();
    bindFabricatorFactory();
    bindIngestFactory();
    bindSegmentFactory();
    install(new MixerModule());
  }

  /**
   [#166317849] Segment is an interface provided by a SegmentFactory
   */
  private void bindSegmentFactory() {
    install(new FactoryModuleBuilder()
      .implement(Segment.class, SegmentImpl.class)
      .build(SegmentFactory.class));
  }

  private void bindApp() {
    bind(AccessControlProvider.class).to(AccessControlProviderImpl.class);
    bind(AccessLogFilterProvider.class).to(AccessLogFilterProviderImpl.class);
    bind(AccessTokenAuthFilter.class).to(AccessTokenAuthFilterImpl.class);
    bind(App.class).to(AppImpl.class);
    bind(AudioCacheProvider.class).to(AudioCacheProviderImpl.class);
    bind(DataStoreFactory.class).to(MemoryDataStoreFactory.class);
    bind(Health.class).to(HealthImpl.class);
    bind(Heartbeat.class).to(HeartbeatImpl.class);
    bind(HttpResponseProvider.class).to(HttpResponseProviderImpl.class);
    bind(HttpServerProvider.class).to(HttpServerProviderImpl.class);
    bind(HttpTransport.class).to(NetHttpTransport.class);
    bind(JsonFactory.class).to(JacksonFactory.class);
    bind(JsonFactory.class).to(JacksonFactory.class);
    bind(GsonProvider.class).to(GsonProviderImpl.class);
    bind(RedisDatabaseProvider.class).to(RedisDatabaseProviderImpl.class);
    bind(ResourceConfigProvider.class).to(ResourceConfigProviderImpl.class);
    bind(SQLDatabaseProvider.class).to(SQLDatabaseProviderImpl.class);
    bind(StatsProvider.class).to(StatsProviderImpl.class);
    bind(TokenGenerator.class).to(TokenGeneratorImpl.class);
    bind(WorkManager.class).to(WorkManagerImpl.class);
  }

  private void bindDAO() {
    bind(AccountDAO.class).to(AccountDAOImpl.class);
    bind(AccountUserDAO.class).to(AccountUserDAOImpl.class);
    bind(AudioChordDAO.class).to(AudioChordDAOImpl.class);
    bind(AudioDAO.class).to(AudioDAOImpl.class);
    bind(AudioEventDAO.class).to(AudioEventDAOImpl.class);
    bind(ChainConfigDAO.class).to(ChainConfigDAOImpl.class);
    bind(ChainDAO.class).to(ChainDAOImpl.class);
    bind(ChainInstrumentDAO.class).to(ChainInstrumentDAOImpl.class);
    bind(ChainLibraryDAO.class).to(ChainLibraryDAOImpl.class);
    bind(ChainSequenceDAO.class).to(ChainSequenceDAOImpl.class);
    bind(InstrumentDAO.class).to(InstrumentDAOImpl.class);
    bind(InstrumentMemeDAO.class).to(InstrumentMemeDAOImpl.class);
    bind(LibraryDAO.class).to(LibraryDAOImpl.class);
    bind(PatternChordDAO.class).to(PatternChordDAOImpl.class);
    bind(PatternDAO.class).to(PatternDAOImpl.class);
    bind(PatternEventDAO.class).to(PatternEventDAOImpl.class);
    bind(SequencePatternMemeDAO.class).to(SequencePatternMemeDAOImpl.class);
    bind(PlatformMessageDAO.class).to(PlatformMessageDAOImpl.class);
    bind(SegmentDAO.class).to(SegmentDAOImpl.class);
    bind(SequenceDAO.class).to(SequenceDAOImpl.class);
    bind(SequenceMemeDAO.class).to(SequenceMemeDAOImpl.class);
    bind(SequencePatternDAO.class).to(SequencePatternDAOImpl.class);
    bind(UserDAO.class).to(UserDAOImpl.class);
    bind(VoiceDAO.class).to(VoiceDAOImpl.class);
  }

  private void bindExternal() {
    bind(AmazonProvider.class).to(AmazonProviderImpl.class);
    bind(GoogleHttpProvider.class).to(GoogleHttpProviderImpl.class);
    bind(GoogleProvider.class).to(GoogleProviderImpl.class);
  }

  private void bindFabricatorFactory() {
    install(new FactoryModuleBuilder()
      .implement(Fabricator.class, FabricatorImpl.class)
      .build(FabricatorFactory.class));
  }

  private void bindIngestFactory() {
    bind(IngestCacheProvider.class).to(IngestCacheProviderImpl.class);
    install(new FactoryModuleBuilder()
      .implement(Ingest.class, IngestImpl.class)
      .build(IngestFactory.class));
  }

}
