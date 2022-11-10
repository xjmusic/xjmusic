// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.zaxxer.hikari.HikariDataSource;
import io.xj.hub.access.GoogleProvider;
import io.xj.hub.access.HubAccessControlProvider;
import io.xj.hub.analysis.HubAnalysisModule;
import io.xj.hub.kubernetes.KubernetesAdmin;
import io.xj.hub.manager.*;
import io.xj.hub.ingest.HubIngestFactory;
import io.xj.hub.persistence.HubDatabaseProvider;
import io.xj.hub.persistence.HubMigration;
import io.xj.lib.app.App;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreProvider;
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
  public AccountManager accountManager;
  @Mock
  public AccountUserManager accountUserManager;
  @Mock
  public FileStoreProvider fileStoreProvider;
  @Mock
  public GoogleProvider googleProvider;
  @Mock
  public HikariDataSource mockDataSource;
  @Mock
  public HubMigration hubMigration;
  @Mock
  public HubAccessControlProvider hubAccessControlProvider;
  @Mock
  public HubDatabaseProvider hubDatabaseProvider;
  @Mock
  public HubIngestFactory hubIngestFactory;
  @Mock
  public InstrumentAudioManager instrumentAudioManager;
  @Mock
  public InstrumentManager instrumentManager;
  @Mock
  public InstrumentMemeManager instrumentMemeManager;
  @Mock
  public KubernetesAdmin kubernetesAdmin;
  @Mock
  public LibraryManager libraryManager;
  @Mock
  public ProgramManager programManager;
  @Mock
  public ProgramMemeManager programMemeManager;
  @Mock
  public ProgramSequencePatternEventManager programSequencePatternEventManager;
  @Mock
  public ProgramSequencePatternManager programSequencePatternManager;
  @Mock
  public ProgramSequenceManager programSequenceManager;
  @Mock
  public ProgramSequenceBindingManager programSequenceBindingManager;
  @Mock
  public ProgramSequenceBindingMemeManager programSequenceBindingMemeManager;
  @Mock
  public ProgramSequenceChordManager programSequenceChordManager;
  @Mock
  public ProgramSequenceChordVoicingManager programSequenceChordVoicingManager;
  @Mock
  public ProgramVoiceTrackManager programVoiceTrackManager;
  @Mock
  public ProgramVoiceManager programVoiceManager;
  @Mock
  public TemplateManager templateManager;
  @Mock
  public TemplateBindingManager templateBindingManager;
  @Mock
  public TemplatePlaybackManager templatePlaybackManager;
  @Mock
  public TemplatePublicationManager templatePublicationManager;
  @Mock
  public UserManager userManager;

  @Before
  public void setUp() throws Exception {
    httpClient = HttpClients.createDefault();
    var env = Environment.from(ImmutableMap.of("APP_PORT", "1903"));
    env.setAppName("hub");
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new JsonapiModule(), new HubAnalysisModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(AccountManager.class).toInstance(accountManager);
        bind(AccountUserManager.class).toInstance(accountUserManager);
        bind(Environment.class).toInstance(env);
        bind(FileStoreProvider.class).toInstance(fileStoreProvider);
        bind(GoogleProvider.class).toInstance(googleProvider);
        bind(HubAccessControlProvider.class).toInstance(hubAccessControlProvider);
        bind(HubDatabaseProvider.class).toInstance(hubDatabaseProvider);
        bind(HubIngestFactory.class).toInstance(hubIngestFactory);
        bind(HubMigration.class).toInstance(hubMigration);
        bind(InstrumentAudioManager.class).toInstance(instrumentAudioManager);
        bind(InstrumentManager.class).toInstance(instrumentManager);
        bind(InstrumentMemeManager.class).toInstance(instrumentMemeManager);
        bind(KubernetesAdmin.class).toInstance(kubernetesAdmin);
        bind(LibraryManager.class).toInstance(libraryManager);
        bind(ProgramManager.class).toInstance(programManager);
        bind(ProgramMemeManager.class).toInstance(programMemeManager);
        bind(ProgramSequenceBindingManager.class).toInstance(programSequenceBindingManager);
        bind(ProgramSequenceBindingMemeManager.class).toInstance(programSequenceBindingMemeManager);
        bind(ProgramSequenceChordManager.class).toInstance(programSequenceChordManager);
        bind(ProgramSequenceChordVoicingManager.class).toInstance(programSequenceChordVoicingManager);
        bind(ProgramSequenceManager.class).toInstance(programSequenceManager);
        bind(ProgramSequencePatternEventManager.class).toInstance(programSequencePatternEventManager);
        bind(ProgramSequencePatternManager.class).toInstance(programSequencePatternManager);
        bind(ProgramVoiceManager.class).toInstance(programVoiceManager);
        bind(ProgramVoiceTrackManager.class).toInstance(programVoiceTrackManager);
        bind(TemplateBindingManager.class).toInstance(templateBindingManager);
        bind(TemplateManager.class).toInstance(templateManager);
        bind(TemplatePlaybackManager.class).toInstance(templatePlaybackManager);
        bind(TemplatePublicationManager.class).toInstance(templatePublicationManager);
        bind(UserManager.class).toInstance(userManager);
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
