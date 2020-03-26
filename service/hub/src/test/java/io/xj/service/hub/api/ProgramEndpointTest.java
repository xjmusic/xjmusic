// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.AppException;
import io.xj.lib.rest_api.Payload;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.HubException;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.digest.DigestModule;
import io.xj.service.hub.generation.GenerationModule;
import io.xj.service.hub.model.Account;
import io.xj.service.hub.model.Library;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramState;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.User;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.InternalResources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Collection;

import static io.xj.service.hub.access.Access.CONTEXT_KEY;
import static io.xj.service.hub.testing.AssertPayload.assertPayload;
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
  private Access access;
  private ProgramEndpoint subject;
  private User user101;
  private Library library25;
  private Library library1;

  @Before
  public void setUp() throws AppException {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of((Modules.override(new HubModule(), new DigestModule(), new GenerationModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ProgramDAO.class).toInstance(programDAO);
        }
      }))));
    HubApp.buildApiTopology(injector.getInstance(PayloadFactory.class));
    Account account1 = Account.create();
    access = Access.create(ImmutableList.of(account1), "User,Artist");
    user101 = User.create();
    library25 = Library.create();
    library1 = Library.create();
    subject = new ProgramEndpoint(injector);
    injector.injectMembers(subject);
  }

  @Test
  public void readAll() throws HubException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program program1 = Program.create(user101, library25, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
    Program program2 = Program.create(user101, library25, ProgramType.Main, ProgramState.Published, "trunk", "B", 120.0, 0.6);
    Collection<Program> programs = ImmutableList.of(program1, program2);
    when(programDAO.readMany(same(access), eq(ImmutableList.of(library25.getId()))))
      .thenReturn(programs);

    Response result = subject.readAll(crc, null, library25.getId().toString(), "");

    verify(programDAO).readMany(same(access), eq(ImmutableList.of(library25.getId())));
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(InternalResources.deserializePayload(result.getEntity()))
      .hasDataMany("programs", ImmutableList.of(program1.getId().toString(), program2.getId().toString()));
  }

  @Test
  public void readOne() throws HubException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Program program1 = Program.create(user101, library1, ProgramType.Main, ProgramState.Published, "fonds", "C#", 120.0, 0.6);
    when(programDAO.readOne(same(access), eq(program1.getId()))).thenReturn(program1);

    Response result = subject.readOne(crc, program1.getId().toString(), "");

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    Payload resultPayload = InternalResources.deserializePayload(result.getEntity());
    assertPayload(resultPayload)
      .hasDataOne("programs", program1.getId().toString());
  }
}
