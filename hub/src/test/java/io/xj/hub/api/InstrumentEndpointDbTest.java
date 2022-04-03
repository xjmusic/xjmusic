// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubIntegrationTestModule;
import io.xj.hub.HubIntegrationTestProvider;
import io.xj.hub.IntegrationTestingFixtures;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.ManagerModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.jsonapi.JsonapiPayload;
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

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildAccountUser;
import static io.xj.hub.IntegrationTestingFixtures.buildInstrument;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildUser;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstrumentEndpointDbTest {
  @Mock
  ContainerRequestContext context;
  private InstrumentEndpoint subject;
  private HubIntegrationTestProvider test;
  private IntegrationTestingFixtures fake;
  private JsonapiPayloadFactory jsonapiPayloadFactory;
  private JsonProvider jsonProvider;

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
    // Account "palm tree" has instrument "leaves" and instrument InstrumentType.Negative
    fake.account1 = test.insert(buildAccount("palm tree"));
    fake.library1 = test.insert(buildLibrary(fake.account1, "sandwich"));
    fake.instrument201 = test.insert(buildInstrument(fake.library1, InstrumentType.Pad, InstrumentMode.NoteEvent, InstrumentState.Published, "buns"));
    fake.instrument202 = test.insert(buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.NoteEvent, InstrumentState.Published, "jams"));
    //
    // User in account
    fake.user1 = test.insert(buildUser("jim", "jim@jim.com", "https://www.jim.com/jim.png", "User"));
    test.insert(buildAccountUser(fake.account1, fake.user1));
    //
    // Instantiate the test subject
    jsonapiPayloadFactory = injector.getInstance(JsonapiPayloadFactory.class);
    jsonProvider = injector.getInstance(JsonProvider.class);
    subject = injector.getInstance(InstrumentEndpoint.class);
  }

  //
  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void readMany_forLibrary() throws JsonapiException {
    when(context.getProperty(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    Response result = subject.readMany(context, null, fake.library1.getId(), null);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    var resultPayload = jsonapiPayloadFactory.deserialize(String.valueOf(result.getEntity()));
    assertEquals(PayloadDataType.Many, resultPayload.getDataType());
    assertEquals(2, resultPayload.getDataMany().size());
  }

  @Test
  public void readMany_forAccount() throws JsonapiException {
    when(context.getProperty(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    Response result = subject.readMany(context, fake.account1.getId(), null, null);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    var resultPayload = jsonapiPayloadFactory.deserialize(String.valueOf(result.getEntity()));
    assertEquals(PayloadDataType.Many, resultPayload.getDataType());
    assertEquals(2, resultPayload.getDataMany().size());
  }

  @Test
  public void create() throws ManagerException, IOException, JsonapiException {
    var toCreate = buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.NoteEvent, InstrumentState.Published, "test");
    var input = jsonapiPayloadFactory.from(toCreate);
    when(context.getProperty(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    Response result = subject.create(context, input, null);

    assertEquals(201, result.getStatus());
    var resultPayload = jsonapiPayloadFactory.deserialize(String.valueOf(result.getEntity()));
    assertEquals(PayloadDataType.One, resultPayload.getDataType());
  }

  /**
   Lab entity attribute invalidations should throw clean errors in api payload
   https://www.pivotaltracker.com/story/show/181516000
   */
  @Test
  public void create_invalidThrowsCleanErrorPayload() throws JsonapiException, IOException {
    var toCreate = buildInstrument(fake.library1, InstrumentType.Drum, InstrumentMode.NoteEvent, InstrumentState.Published, ""); // empty name not allowed
    var input = jsonapiPayloadFactory.from(toCreate);
    when(context.getProperty(CONTEXT_KEY)).thenReturn(HubAccess.internal());

    Response response = subject.create(context, input, null);

    assertEquals(406, response.getStatus());
    var result = jsonProvider.getMapper().readValue(String.valueOf(response.getEntity()), JsonapiPayload.class);
    assertPayload(result).hasErrorCount(1);
    var error = result.getErrors().iterator().next();
    assertEquals("Name is required.", error.getTitle());
  }

}
