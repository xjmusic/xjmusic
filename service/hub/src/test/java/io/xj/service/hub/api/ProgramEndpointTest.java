// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.pubsub.FileStoreModule;
import io.xj.lib.jsonapi.Payload;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.dao.DAOException;
import io.xj.service.hub.dao.DAOModule;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.digest.HubDigestModule;
import io.xj.service.hub.entity.*;
import io.xj.service.hub.generation.HubGenerationModule;
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
  private User user101;
  private Library library25;
  private Library library1;

  @Before
  public void setUp() throws AppException {
    Config config = HubTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of((Modules.override(new HubAccessControlModule(), new DAOModule(), new HubIngestModule(), new HubPersistenceModule(), new MixerModule(), new JsonApiModule(), new FileStoreModule(), new HubDigestModule(), new HubGenerationModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ProgramDAO.class).toInstance(programDAO);
        }
      }))));
    HubApp.buildApiTopology(injector.getInstance(EntityFactory.class));
    Account account1 = Account.create();
    hubAccess = HubAccess.create(ImmutableList.of(account1), "User,Artist");
    user101 = User.create();
    library25 = Library.create();
    library1 = Library.create();
    subject = new ProgramEndpoint(injector);
    injector.injectMembers(subject);
  }

  @Test
  public void readMany() throws DAOException, IOException, JsonApiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    Program program1 = Program.create(user101, library25, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
    Program program2 = Program.create(user101, library25, ProgramType.Main, ProgramState.Published, "trunk", "B", 120.0, 0.6);
    Collection<Program> programs = ImmutableList.of(program1, program2);
    when(programDAO.readMany(same(hubAccess), eq(ImmutableList.of(library25.getId()))))
      .thenReturn(programs);

    Response result = subject.readMany(crc, null, library25.getId().toString(), false);

    verify(programDAO).readMany(same(hubAccess), eq(ImmutableList.of(library25.getId())));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(new ObjectMapper().readValue(String.valueOf(result.getEntity()), Payload.class))
      .hasDataMany("programs", ImmutableList.of(program1.getId().toString(), program2.getId().toString()));
  }

  @Test
  public void readOne() throws DAOException, IOException, JsonApiException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(hubAccess);
    Program program1 = Program.create(user101, library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
    when(programDAO.readOne(same(hubAccess), eq(program1.getId()))).thenReturn(program1);

    Response result = subject.readOne(crc, program1.getId().toString(), "");

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload resultPayload = new ObjectMapper().readValue(String.valueOf(result.getEntity()), Payload.class);
    assertPayload(resultPayload)
      .hasDataOne("programs", program1.getId().toString());
  }
}
