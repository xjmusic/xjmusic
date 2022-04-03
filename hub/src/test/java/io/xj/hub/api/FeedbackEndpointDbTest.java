// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubException;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.ManagerModule;
import io.xj.hub.enums.*;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.PayloadDataType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FeedbackEndpointDbTest {
  @Mock
  ContainerRequestContext context;
  private FeedbackEndpoint subject;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;
  private JsonapiPayloadFactory jsonapiPayloadFactory;

  @Before
  public void setUp() throws Exception {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule(), new HubIntegrationTestModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
      }
    }));
    test = injector.getInstance(HubIntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);
    //
    test.reset();
    //
    // Account "palm tree" has feedback "leaves" and feedback FeedbackType.Negative
    fake.account1 = test.insert(buildAccount("palm tree"));
    fake.feedback1a = test.insert(buildFeedback(fake.account1, FeedbackType.Negative, FeedbackSource.Listener));
    fake.feedback1b = test.insert(buildFeedback(fake.account1, FeedbackType.Positive, FeedbackSource.Artist));
    //
    // User in account
    fake.user1 = test.insert(buildUser("jim", "jim@jim.com", "https://www.jim.com/jim.png", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user1));
    //
    // Instantiate the test subject
    jsonapiPayloadFactory = injector.getInstance(JsonapiPayloadFactory.class);
    subject = injector.getInstance(FeedbackEndpoint.class);
  }

  //
  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void readMany() throws ManagerException, IOException, JsonapiException {
    when(context.getProperty(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    Response result = subject.readMany(context, fake.account1.getId(), null, null, null, null, null);

    assertEquals(201, result.getStatus());
    assertTrue(result.hasEntity());
    var resultPayload = jsonapiPayloadFactory.deserialize(String.valueOf(result.getEntity()));
    assertEquals(PayloadDataType.Many, resultPayload.getDataType());
    assertEquals(2, resultPayload.getDataMany().size());
  }

  @Test
  public void readMany_forInstrument() throws JsonapiException, HubException {
    fake.library1 = test.insert(buildLibrary(fake.account1, "sandwich"));
    fake.instrument201 = test.insert(buildInstrument(fake.library1, InstrumentType.Pad, InstrumentMode.NoteEvent, InstrumentState.Published, "buns"));
    fake.instrument202 = test.insert(buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.NoteEvent, InstrumentState.Published, "jams"));
    fake.audio1 = test.insert(buildInstrumentAudio(fake.instrument202, "Test audio", "fake.audio5.wav", 0.0f, 2.0f, 120.0f));
    test.insert(buildFeedbackInstrument(fake.feedback1a, fake.instrument201));
    test.insert(buildFeedbackInstrument(fake.feedback1b, fake.instrument202));
    when(context.getProperty(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    Response result = subject.readMany(context, fake.account1.getId(), fake.instrument201.getId(), null, null, null, null);

    assertEquals(201, result.getStatus());
    assertTrue(result.hasEntity());
    var resultPayload = jsonapiPayloadFactory.deserialize(String.valueOf(result.getEntity()));
    assertEquals(PayloadDataType.Many, resultPayload.getDataType());
    assertEquals(1, resultPayload.getDataMany().size());
  }

  @Test
  public void readMany_forProgram() throws JsonapiException, HubException {
    fake.library1 = test.insert(buildLibrary(fake.account1, "palm tree"));
    fake.program1 = test.insert(buildProgram(fake.library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0f, 0.6f));
    fake.program1_sequence1 = test.insert(buildProgramSequence(fake.program1, 4, "Ants", 0.583f, "D minor"));
    var sequenceBinding1a_0 = test.insert(buildProgramSequenceBinding(fake.program1_sequence1, 0));
    test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "chunk"));
    test.insert(buildProgramSequenceBindingMeme(sequenceBinding1a_0, "smooth"));
    fake.program2 = test.insert(buildProgram(fake.library1, ProgramType.Beat, ProgramState.Published, "nuts", "C#", 120.0f, 0.6f));
    test.insert(buildFeedbackProgram(fake.feedback1a, fake.program1));
    test.insert(buildFeedbackProgram(fake.feedback1b, fake.program2));
    when(context.getProperty(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    Response result = subject.readMany(context, fake.account1.getId(), null, null, fake.program1.getId(), null, null);

    assertEquals(201, result.getStatus());
    assertTrue(result.hasEntity());
    var resultPayload = jsonapiPayloadFactory.deserialize(String.valueOf(result.getEntity()));
    assertEquals(PayloadDataType.Many, resultPayload.getDataType());
    assertEquals(1, resultPayload.getDataMany().size());
  }

  @Test
  public void readMany_forTemplate() throws JsonapiException, HubException {
    fake.template1 = test.insert(buildTemplate(fake.account1, "leaves", "embed5leaves"));
    fake.template2 = test.insert(buildTemplate(fake.account1, "coconuts", "embed5coconuts"));
    test.insert(buildFeedbackTemplate(fake.feedback1a, fake.template1, "things"));
    test.insert(buildFeedbackTemplate(fake.feedback1b, fake.template2, "other5things"));
    when(context.getProperty(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    Response result = subject.readMany(context, fake.account1.getId(), null, null, null, fake.template1.getId(), null);

    assertEquals(201, result.getStatus());
    assertTrue(result.hasEntity());
    var resultPayload = jsonapiPayloadFactory.deserialize(String.valueOf(result.getEntity()));
    assertEquals(PayloadDataType.Many, resultPayload.getDataType());
    assertEquals(1, resultPayload.getDataMany().size());
  }

  @Test
  public void readMany_forUser() throws JsonapiException, HubException {
    test.insert(buildFeedback(fake.account1, FeedbackType.Positive, FeedbackSource.Artist, fake.user1));
    when(context.getProperty(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    Response result = subject.readMany(context, fake.account1.getId(), null, null, null, null, fake.user1.getId());

    assertEquals(201, result.getStatus());
    assertTrue(result.hasEntity());
    var resultPayload = jsonapiPayloadFactory.deserialize(String.valueOf(result.getEntity()));
    assertEquals(PayloadDataType.Many, resultPayload.getDataType());
    assertEquals(1, resultPayload.getDataMany().size());
  }

  @Test
  public void create() throws ManagerException, IOException, JsonapiException {
    var toCreate = buildFeedback(fake.account1, FeedbackType.Positive, FeedbackSource.Artist, fake.user1);
    var input = jsonapiPayloadFactory.from(toCreate);
    when(context.getProperty(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    Response result = subject.create(context, input);

    assertEquals(201, result.getStatus());
    var resultPayload = jsonapiPayloadFactory.deserialize(String.valueOf(result.getEntity()));
    assertEquals(PayloadDataType.One, resultPayload.getDataType());
  }

}
