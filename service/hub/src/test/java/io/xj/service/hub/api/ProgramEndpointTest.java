// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.Library;
import io.xj.Program;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.dao.DAOException;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import static io.xj.lib.jsonapi.AssertPayload.assertPayload;
import static io.xj.service.hub.access.HubAccess.CONTEXT_KEY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ProgramEndpointTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  ProgramDAO programDAO;
  private HubAccess hubAccess;
  private ProgramEndpoint subject;
  private Library library25;
  private Library library1;

  @Before
  public void setUp() throws AppException {
    Config config = HubTestConfiguration.getDefault();
    var injector = AppConfiguration.inject(config, ImmutableSet.of((Modules.override(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ProgramDAO.class).toInstance(programDAO);
        }
      }))));
    HubApp.buildApiTopology(injector.getInstance(EntityFactory.class));
    var account1 = Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    hubAccess = HubAccess.create(ImmutableList.of(account1), "User,Artist");
    library25 = Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    library1 = Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    subject = injector.getInstance(ProgramEndpoint.class);
    injector.injectMembers(subject);
  }

  @Test
  public void readMany() throws DAOException, IOException, JsonApiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    Program program1 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(library25.getId())
      .setType(Program.Type.Main)
      .setState(Program.State.Published)
      .setName("fonds")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build();
    Program program2 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(library25.getId())
      .setType(Program.Type.Main)
      .setState(Program.State.Published)
      .setName("trunk")
      .setKey("B")
      .setTempo(120.0)
      .setDensity(0.6)
      .build();
    Collection<Program> programs = ImmutableList.of(program1, program2);
    when(programDAO.readMany(same(hubAccess), eq(ImmutableList.of(library25.getId()))))
      .thenReturn(programs);

    Response result = subject.readMany(crc, null, library25.getId(), false);

    verify(programDAO).readMany(same(hubAccess), eq(ImmutableList.of(library25.getId())));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), Payload.class))
      .hasDataMany("programs", ImmutableList.of(program1.getId(), program2.getId()));
  }

  @Test
  public void readOne() throws DAOException, IOException, JsonApiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    Program program1 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(library1.getId())
      .setType(Program.Type.Main)
      .setState(Program.State.Published)
      .setName("fonds")
      .setKey("C#")
      .setTempo(120.0)
      .setDensity(0.6)
      .build();
    when(programDAO.readOne(same(hubAccess), eq(program1.getId()))).thenReturn(program1);

    Response result = subject.readOne(crc, program1.getId(), "");

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload resultPayload = new ObjectMapper().readValue(String.valueOf(result.getEntity()), Payload.class);
    assertPayload(resultPayload)
      .hasDataOne("programs", program1.getId());
  }
}
