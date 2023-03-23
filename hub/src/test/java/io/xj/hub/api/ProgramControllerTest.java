// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.api;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubTopology;
import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.manager.ProgramManager;
import io.xj.hub.manager.ProgramMemeManager;
import io.xj.hub.manager.ProgramSequenceBindingMemeManager;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import io.xj.lib.jsonapi.JsonapiResponseProviderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.access.HubAccess.CONTEXT_KEY;
import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
public class ProgramControllerTest {
  @Mock
  HttpServletRequest req;
  @Mock
  ProgramManager programManager;
  @Mock
  private HubSqlStoreProvider sqlStoreProvider;
  @Mock
  ProgramMemeManager programMemeManager;
  @Mock
  ProgramSequenceBindingMemeManager programSequenceBindingMemeManager;
  private HubAccess access;
  private ProgramController subject;
  private Library library25;
  private Library library1;

  @BeforeEach
  public void setUp() throws AppException {
    var env = AppEnvironment.getDefault();
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    JsonapiPayloadFactory payloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    HubTopology.buildHubApiTopology(entityFactory);
    ApiUrlProvider apiUrlProvider = new ApiUrlProvider(env);
    JsonapiResponseProvider responseProvider = new JsonapiResponseProviderImpl(apiUrlProvider);

    var account1 = buildAccount("Testing");
    access = HubAccess.create(UUID.randomUUID(), UUID.randomUUID(), ImmutableList.of(account1), "User,Artist");
    library25 = buildLibrary(account1, "Test 25");
    library1 = buildLibrary(account1, "Test 1");
    subject = new ProgramController(programManager, programSequenceBindingMemeManager, programMemeManager, sqlStoreProvider, responseProvider, payloadFactory, entityFactory);
  }

  @Test
  public void readMany() throws ManagerException, IOException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
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
    when(programManager.readMany(same(access), eq(ImmutableList.of(library25.getId()))))
      .thenReturn(programs);

    var result = subject.readMany(req, null, library25.getId(), false);

    verify(programManager).readMany(same(access), eq(ImmutableList.of(library25.getId())));
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    assertPayload(result.getBody())
      .hasDataMany("programs", ImmutableList.of(program1.getId().toString(), program2.getId().toString()));
  }

  @Test
  public void readOne() throws ManagerException, IOException, JsonapiException {
    when(req.getAttribute(CONTEXT_KEY)).thenReturn(access);
    Program program1 = new Program();
    program1.setId(UUID.randomUUID());
    program1.setLibraryId(library1.getId());
    program1.setType(ProgramType.Main);
    program1.setState(ProgramState.Published);
    program1.setName("fonds");
    program1.setKey("C#");
    program1.setTempo(120.0f);
    program1.setDensity(0.6f);
    when(programManager.readOne(same(access), eq(program1.getId()))).thenReturn(program1);

    var result = subject.readOne(req, program1.getId(), "");

    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertTrue(result.hasBody());
    JsonapiPayload resultJsonapiPayload = result.getBody();
    assertPayload(resultJsonapiPayload)
      .hasDataOne("programs", program1.getId().toString());
  }
}
