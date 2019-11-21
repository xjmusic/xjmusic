//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.resource.chain;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.CoreTest;
import io.xj.core.access.Access;
import io.xj.core.dao.ChainDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;

import static io.xj.core.access.Access.CONTEXT_KEY;
import static io.xj.core.testing.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChainIndexResourceTest extends CoreTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  ChainDAO chainDAO;
  private Access access;
  private ChainIndexResource subject;
  private Account account25;

  @Before
  public void setUp() {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new CraftModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ChainDAO.class).toInstance(chainDAO);
        }
      }));
    account25 = Account.create();
    access = Access.create(ImmutableList.of(account25), "User,Artist");
    subject = new ChainIndexResource();
    subject.setInjector(injector);
  }

  @Test
  public void readAll() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Chain chain1;
    Chain chain2;
    chain1 = Chain.create(account25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);
    chain2 = Chain.create(account25, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-09-12T12:17:02.527142Z"), null, null);
    Collection<Chain> chains = ImmutableList.of(chain1, chain2);
    when(chainDAO.readMany(same(access), eq(ImmutableList.of(account25.getId()))))
      .thenReturn(chains);
    subject.accountId = account25.getId().toString();

    Response result = subject.readAll(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataMany("chains", ImmutableList.of(chain1.getId().toString(), chain2.getId().toString()));
  }
}
