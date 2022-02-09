// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubTopology;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlModule;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.ManagerModule;
import io.xj.hub.manager.ProgramManager;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.ingest.HubIngestModule;
import io.xj.hub.persistence.HubPersistenceModule;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.lib.app.AppException;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.jsonapi.JsonapiPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProgramEndpointTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  ProgramManager programManager;
  private HubAccess hubAccess;
  private ProgramEndpoint subject;
  private Library library25;
  private Library library1;

  @Before
  public void setUp() throws AppException {
    var env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(ImmutableSet.of(new HubAccessControlModule(), new ManagerModule(), new HubIngestModule(), new HubPersistenceModule(), new JsonapiModule(), new FileStoreModule())).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(Environment.class).toInstance(env);
        bind(ProgramManager.class).toInstance(programManager);
      }
    }));

    HubTopology.buildHubApiTopology(injector.getInstance(EntityFactory.class));
    var account1 = buildAccount("Testing");
    hubAccess = HubAccess.create(ImmutableList.of(account1), "User,Artist");
    library25 = buildLibrary(account1, "Test 25");
    library1 = buildLibrary(account1, "Test 1");
    subject = injector.getInstance(ProgramEndpoint.class);
    injector.injectMembers(subject);
  }

  @Test
  public void readMany() throws ManagerException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    Program program1 = new Program();
    program1.setId(UUID.randomUUID());
    program1.setLibraryId(library25.getId());
    program1.setType(ProgramType.Main);
    program1.setState(ProgramState.Published);
    program1.setName("fonds");
    program1.setKey("C#");
    program1.setTempo(120.0f);
    program1.setDensity(0.6f);
    Program program2 = new Program();
    program2.setId(UUID.randomUUID());
    program2.setLibraryId(library25.getId());
    program2.setType(ProgramType.Main);
    program2.setState(ProgramState.Published);
    program2.setName("trunk");
    program2.setKey("B");
    program2.setTempo(120.0f);
    program2.setDensity(0.6f);
    Collection<Program> programs = ImmutableList.of(program1, program2);
    when(programManager.readMany(same(hubAccess), eq(ImmutableList.of(library25.getId()))))
      .thenReturn(programs);

    Response result = subject.readMany(crc, null, library25.getId().toString(), false);

    verify(programManager).readMany(same(hubAccess), eq(ImmutableList.of(library25.getId())));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class))
      .hasDataMany("programs", ImmutableList.of(program1.getId().toString(), program2.getId().toString()));
  }

  @Test
  public void readOne() throws ManagerException, IOException, JsonapiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    Program program1 = new Program();
    program1.setId(UUID.randomUUID());
    program1.setLibraryId(library1.getId());
    program1.setType(ProgramType.Main);
    program1.setState(ProgramState.Published);
    program1.setName("fonds");
    program1.setKey("C#");
    program1.setTempo(120.0f);
    program1.setDensity(0.6f);
    when(programManager.readOne(same(hubAccess), eq(program1.getId()))).thenReturn(program1);

    Response result = subject.readOne(crc, program1.getId().toString(), "");

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    JsonapiPayload resultJsonapiPayload = new ObjectMapper().readValue(String.valueOf(result.getEntity()), JsonapiPayload.class);
    assertPayload(resultJsonapiPayload)
      .hasDataOne("programs", program1.getId().toString());
  }
}
