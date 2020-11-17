// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.app.App;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.service.hub.access.GoogleProvider;
import io.xj.service.hub.access.HubAccessControlProvider;
import io.xj.service.hub.dao.AccountDAO;
import io.xj.service.hub.dao.AccountUserDAO;
import io.xj.service.hub.dao.InstrumentAudioChordDAO;
import io.xj.service.hub.dao.InstrumentAudioDAO;
import io.xj.service.hub.dao.InstrumentAudioEventDAO;
import io.xj.service.hub.dao.InstrumentDAO;
import io.xj.service.hub.dao.InstrumentMemeDAO;
import io.xj.service.hub.dao.LibraryDAO;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.dao.ProgramMemeDAO;
import io.xj.service.hub.dao.ProgramSequenceBindingDAO;
import io.xj.service.hub.dao.ProgramSequenceBindingMemeDAO;
import io.xj.service.hub.dao.ProgramSequenceChordDAO;
import io.xj.service.hub.dao.ProgramSequenceChordVoicingDAO;
import io.xj.service.hub.dao.ProgramSequenceDAO;
import io.xj.service.hub.dao.ProgramSequencePatternDAO;
import io.xj.service.hub.dao.ProgramSequencePatternEventDAO;
import io.xj.service.hub.dao.ProgramVoiceDAO;
import io.xj.service.hub.dao.ProgramVoiceTrackDAO;
import io.xj.service.hub.dao.UserDAO;
import io.xj.service.hub.ingest.HubIngestFactory;
import io.xj.service.hub.persistence.HubDatabaseProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.URI;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class HubAppTest {
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
  public InstrumentAudioChordDAO instrumentAudioChordDAO;
  @Mock
  public InstrumentAudioEventDAO instrumentAudioEventDAO;
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
  public UserDAO userDAO;
  @Mock
  private GoogleProvider googleProvider;
  public App subject;
  public CloseableHttpClient httpClient;

  @Before
  public void setUp() throws Exception {
    httpClient = HttpClients.createDefault();
    Config config = AppConfiguration.getDefault()
      .withValue("audio.baseUrl", ConfigValueFactory.fromAnyRef(""))
      .withValue("segment.baseUrl", ConfigValueFactory.fromAnyRef(""))
      .withValue("app.port", ConfigValueFactory.fromAnyRef(1903))
      .withValue("prometheus.enabled", ConfigValueFactory.fromAnyRef(true));
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new JsonApiModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(HubDatabaseProvider.class).toInstance(hubDatabaseProvider);
        bind(HubAccessControlProvider.class).toInstance(hubAccessControlProvider);
        bind(HubIngestFactory.class).toInstance(hubIngestFactory);
        bind(AccountDAO.class).toInstance(accountDAO);
        bind(AccountUserDAO.class).toInstance(accountUserDAO);
        bind(InstrumentDAO.class).toInstance(instrumentDAO);
        bind(InstrumentAudioDAO.class).toInstance(instrumentAudioDAO);
        bind(InstrumentAudioChordDAO.class).toInstance(instrumentAudioChordDAO);
        bind(InstrumentAudioEventDAO.class).toInstance(instrumentAudioEventDAO);
        bind(InstrumentMemeDAO.class).toInstance(instrumentMemeDAO);
        bind(LibraryDAO.class).toInstance(libraryDAO);
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
    subject = new HubApp(injector);
    subject.start();
  }

  @After
  public void tearDown() throws IOException {
    subject.finish();
    httpClient.close();
  }

  @Test
  public void checkApp() throws Exception {
    HttpGet request = new HttpGet(new URI("http://localhost:1903/-/health"));
    CloseableHttpResponse result = httpClient.execute(request);

    assertEquals(200, result.getStatusLine().getStatusCode());

    assertEquals("hub", subject.getName());
  }
}
