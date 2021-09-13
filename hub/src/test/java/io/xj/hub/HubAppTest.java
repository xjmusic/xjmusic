// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import com.zaxxer.hikari.HikariDataSource;
import io.xj.hub.access.GoogleProvider;
import io.xj.hub.access.HubAccessControlProvider;
import io.xj.hub.dao.AccountDAO;
import io.xj.hub.dao.AccountUserDAO;
import io.xj.hub.dao.InstrumentAudioDAO;
import io.xj.hub.dao.InstrumentDAO;
import io.xj.hub.dao.InstrumentMemeDAO;
import io.xj.hub.dao.LibraryDAO;
import io.xj.hub.dao.ProgramDAO;
import io.xj.hub.dao.ProgramMemeDAO;
import io.xj.hub.dao.ProgramSequenceBindingDAO;
import io.xj.hub.dao.ProgramSequenceBindingMemeDAO;
import io.xj.hub.dao.ProgramSequenceChordDAO;
import io.xj.hub.dao.ProgramSequenceChordVoicingDAO;
import io.xj.hub.dao.ProgramSequenceDAO;
import io.xj.hub.dao.ProgramSequencePatternDAO;
import io.xj.hub.dao.ProgramSequencePatternEventDAO;
import io.xj.hub.dao.ProgramVoiceDAO;
import io.xj.hub.dao.ProgramVoiceTrackDAO;
import io.xj.hub.dao.TemplateBindingDAO;
import io.xj.hub.dao.TemplateDAO;
import io.xj.hub.dao.TemplatePlaybackDAO;
import io.xj.hub.dao.UserDAO;
import io.xj.hub.ingest.HubIngestFactory;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubMigration;
import io.xj.lib.app.App;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.jsonapi.JsonapiModule;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HubAppTest {
  public CloseableHttpClient httpClient;
  public App subject;
  @Mock
  public HubDatabaseProvider hubDatabaseProvider;
  @Mock
  public HubAccessControlProvider hubAccessControlProvider;
  @Mock
  public HubIngestFactory hubIngestFactory;
  @Mock
  public AccountDAO accountDAO;
  @Mock
  public AccountUserDAO accountUserDAO;
  @Mock
  public InstrumentDAO instrumentDAO;
  @Mock
  public InstrumentAudioDAO instrumentAudioDAO;
  @Mock
  public InstrumentMemeDAO instrumentMemeDAO;
  @Mock
  public LibraryDAO libraryDAO;
  @Mock
  public ProgramDAO programDAO;
  @Mock
  public ProgramSequencePatternEventDAO programSequencePatternEventDAO;
  @Mock
  public ProgramMemeDAO programMemeDAO;
  @Mock
  public ProgramSequencePatternDAO programSequencePatternDAO;
  @Mock
  public ProgramSequenceDAO programSequenceDAO;
  @Mock
  public ProgramSequenceBindingDAO programSequenceBindingDAO;
  @Mock
  public ProgramSequenceBindingMemeDAO programSequenceBindingMemeDAO;
  @Mock
  public ProgramSequenceChordDAO programSequenceChordDAO;
  @Mock
  public ProgramSequenceChordVoicingDAO programSequenceChordVoicingDAO;
  @Mock
  public ProgramVoiceTrackDAO programVoiceTrackDAO;
  @Mock
  public ProgramVoiceDAO programVoiceDAO;
  @Mock
  public TemplateDAO templateDAO;
  @Mock
  public TemplateBindingDAO templateBindingDAO;
  @Mock
  public TemplatePlaybackDAO templatePlaybackDAO;
  @Mock
  public UserDAO userDAO;
  @Mock
  private GoogleProvider googleProvider;
  @Mock
  private HikariDataSource mockDataSource;
  @Mock
  private HubMigration hubMigration;

  @Before
  public void setUp() throws Exception {
    httpClient = HttpClients.createDefault();
    Config config = AppConfiguration.getDefault()
      .withValue("audio.baseUrl", ConfigValueFactory.fromAnyRef(""))
      .withValue("segment.baseUrl", ConfigValueFactory.fromAnyRef(""))
      .withValue("app.port", ConfigValueFactory.fromAnyRef(1903));
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new JsonapiModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(config);
        bind(Environment.class).toInstance(env);
        bind(HubDatabaseProvider.class).toInstance(hubDatabaseProvider);
        bind(HubAccessControlProvider.class).toInstance(hubAccessControlProvider);
        bind(HubMigration.class).toInstance(hubMigration);
        bind(HubIngestFactory.class).toInstance(hubIngestFactory);
        bind(AccountDAO.class).toInstance(accountDAO);
        bind(AccountUserDAO.class).toInstance(accountUserDAO);
        bind(InstrumentDAO.class).toInstance(instrumentDAO);
        bind(InstrumentAudioDAO.class).toInstance(instrumentAudioDAO);
        bind(InstrumentMemeDAO.class).toInstance(instrumentMemeDAO);
        bind(LibraryDAO.class).toInstance(libraryDAO);
        bind(TemplateDAO.class).toInstance(templateDAO);
        bind(TemplateBindingDAO.class).toInstance(templateBindingDAO);
        bind(TemplatePlaybackDAO.class).toInstance(templatePlaybackDAO);
        bind(ProgramDAO.class).toInstance(programDAO);
        bind(ProgramSequencePatternEventDAO.class).toInstance(programSequencePatternEventDAO);
        bind(ProgramMemeDAO.class).toInstance(programMemeDAO);
        bind(ProgramSequencePatternDAO.class).toInstance(programSequencePatternDAO);
        bind(ProgramSequenceDAO.class).toInstance(programSequenceDAO);
        bind(ProgramSequenceBindingDAO.class).toInstance(programSequenceBindingDAO);
        bind(ProgramSequenceBindingMemeDAO.class).toInstance(programSequenceBindingMemeDAO);
        bind(ProgramSequenceChordDAO.class).toInstance(programSequenceChordDAO);
        bind(ProgramSequenceChordVoicingDAO.class).toInstance(programSequenceChordVoicingDAO);
        bind(ProgramVoiceTrackDAO.class).toInstance(programVoiceTrackDAO);
        bind(ProgramVoiceDAO.class).toInstance(programVoiceDAO);
        bind(UserDAO.class).toInstance(userDAO);
        bind(GoogleProvider.class).toInstance(googleProvider);
      }
    }));
    when(hubDatabaseProvider.getDataSource()).thenReturn(mockDataSource);
    subject = injector.getInstance(HubApp.class);
    subject.start();
  }

  @After
  public void tearDown() throws IOException {
    subject.finish();
    httpClient.close();
  }

  @Test
  public void checkApp() throws Exception {
    HttpGet request = new HttpGet(new URI("http://localhost:1903/healthz"));
    CloseableHttpResponse result = httpClient.execute(request);

    assertEquals(200, result.getStatusLine().getStatusCode());

    assertEquals("hub", subject.getName());
  }

  @Test
  public void checkApp_failsWithoutDatabaseConnection() throws Exception {
    HttpGet request = new HttpGet(new URI("http://localhost:1903/healthz"));
    doThrow(new SQLException("Failure to connect to database")).when(mockDataSource).getConnection();
    CloseableHttpResponse result = httpClient.execute(request);

    assertEquals(500, result.getStatusLine().getStatusCode());

    assertEquals("hub", subject.getName());
  }
}
