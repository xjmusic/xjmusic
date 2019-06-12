//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.resource.chain;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.CoreTest;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.account.Account;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainConfigType;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain.sub.ChainBinding;
import io.xj.core.model.chain.sub.ChainConfig;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.payload.PayloadObject;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;
import java.time.Instant;
import java.util.UUID;

import static io.xj.core.access.impl.Access.CONTEXT_KEY;
import static io.xj.core.testing.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChainOneResourceTest extends CoreTest {
  @Mock
  ContainerRequestContext crc;
  @Mock
  ChainDAO chainDAO;
  private Access access;
  private ChainOneResource subject;

  @Before
  public void setUp() {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new CraftModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(ChainDAO.class).toInstance(chainDAO);
        }
      }));
    access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "chains", "1"
    ));
    subject = new ChainOneResource();
    subject.setInjector(injector);
  }

  @Test
  public void readOne() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Chain chain17 = newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z"));
    ChainConfig config1 = chain17.add(newChainConfig(ChainConfigType.OutputContainer, "AAC"));
    ChainConfig config2 = chain17.add(newChainConfig(ChainConfigType.OutputChannels, "4"));
    ChainBinding binding1 = chain17.add(newChainBinding("Library", 5));
    when(chainDAO.readOne(same(access), eq(BigInteger.valueOf(17)))).thenReturn(chain17);
    subject.id = "17";

    Response result = subject.readOne(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("chains", "17")
      .hasMany(ChainConfig.class, ImmutableList.of(config1, config2))
      .hasMany(ChainBinding.class, ImmutableList.of(binding1));
  }

  @Test
  public void update() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(chainDAO.readOne(same(access), eq(BigInteger.valueOf(17))))
      .thenReturn(newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z")));
    Chain updated = newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z"));
    Payload payload = new Payload().setDataEntity(updated);
    subject.id = "17";

    Response result = subject.update(payload, crc);

    verify(chainDAO).update(same(access), eq(BigInteger.valueOf(17)), any());
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("chains", "17")
      .hasMany(ChainConfig.class, ImmutableList.of())
      .hasMany(ChainBinding.class, ImmutableList.of())
      .belongsTo(Account.class, "25");
  }

  @Test
  public void update_embeddedEntityPreservesIdFromPayload() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(chainDAO.readOne(same(access), eq(BigInteger.valueOf(17))))
      .thenReturn(newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z")));
    ChainConfig config1 = newChainConfig(ChainConfigType.OutputContainer, "AAC");
    Chain updated = newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z"));
    updated.add(config1);
    Payload payload = new Payload().setDataEntity(updated);
    subject.id = "17";

    Response result = subject.update(payload, crc);

    ArgumentCaptor<Chain> captor = ArgumentCaptor.forClass(Chain.class);
    verify(chainDAO).update(same(access), eq(BigInteger.valueOf(17)), captor.capture());
    assertNotNull(captor.getValue().getConfig(config1.getId()));
    //
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("chains", "17")
      .hasMany(ChainConfig.class, ImmutableList.of(config1))
      .hasMany(ChainBinding.class, ImmutableList.of())
      .belongsTo(Account.class, "25");
  }

  @Test
  public void update_addFirstEmbeddedEntity() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(chainDAO.readOne(same(access), eq(BigInteger.valueOf(17))))
      .thenReturn(newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z")));
    Chain updated = newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z"));
    ChainConfig config1 = updated.add(newChainConfig(ChainConfigType.OutputContainer, "aac"));
    Payload payload = new Payload().setDataEntity(updated);
    subject.id = "17";

    Response result = subject.update(payload, crc);

    ArgumentCaptor<Chain> captor = ArgumentCaptor.forClass(Chain.class);
    verify(chainDAO).update(same(access), eq(BigInteger.valueOf(17)), captor.capture());
    Chain resultChain = captor.getValue();
    assertEquals("Test Print #1", resultChain.getName());
    assertEquals(1, resultChain.getConfigs().size());
    ChainConfig resultChainConfig = resultChain.getConfig(config1.getId());
    assertEquals(ChainConfigType.OutputContainer, resultChainConfig.getType());
    assertEquals("AAC", resultChainConfig.getValue());
    //
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("chains", "17")
      .hasMany(ChainConfig.class, ImmutableList.of(config1))
      .hasMany(ChainBinding.class, ImmutableList.of())
      .belongsTo(Account.class, "25");
  }

  @Test
  public void update_addSecondEmbeddedEntity() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Chain chainBefore = newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z"));
    ChainConfig config1 = chainBefore.add(newChainConfig(ChainConfigType.OutputContainer, "AAC"));
    when(chainDAO.readOne(same(access), eq(BigInteger.valueOf(17))))
      .thenReturn(chainBefore);
    Chain chainAfter = newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z"));
    chainAfter.add(config1);
    ChainConfig config2 = chainAfter.add(newChainConfig(ChainConfigType.OutputChannels, "4"));
    Payload payload = new Payload().setDataEntity(chainAfter);
    subject.id = "17";

    Response result = subject.update(payload, crc);

    ArgumentCaptor<Chain> captor = ArgumentCaptor.forClass(Chain.class);
    verify(chainDAO).update(same(access), eq(BigInteger.valueOf(17)), captor.capture());
    Chain resultChain = captor.getValue();
    assertEquals("Test Print #1", resultChain.getName());
    assertEquals(2, resultChain.getConfigs().size());
    assertNotNull(captor.getValue().getConfig(config1.getId()));
    assertNotNull(captor.getValue().getConfig(config2.getId()));
    //
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("chains", "17")
      .hasMany(ChainConfig.class, ImmutableList.of(config1, config2))
      .hasMany(ChainBinding.class, ImmutableList.of())
      .belongsTo(Account.class, "25");
  }

  @Test
  public void update_removeSecondEmbeddedEntity() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Chain chainBefore = newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z"));
    ChainConfig config1 = chainBefore.add(newChainConfig(ChainConfigType.OutputContainer, "AAC"));
    chainBefore.add(newChainConfig(ChainConfigType.OutputChannels, "4"));
    when(chainDAO.readOne(same(access), eq(BigInteger.valueOf(17))))
      .thenReturn(chainBefore);
    Chain chainAfter = newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z"));
    chainAfter.add(config1);
    Payload payload = new Payload().setDataEntity(chainAfter);
    subject.id = "17";

    Response result = subject.update(payload, crc);

    ArgumentCaptor<Chain> captor = ArgumentCaptor.forClass(Chain.class);
    verify(chainDAO).update(same(access), eq(BigInteger.valueOf(17)), captor.capture());
    Chain resultChain = captor.getValue();
    assertEquals("Test Print #1", resultChain.getName());
    assertEquals(1, resultChain.getConfigs().size());
    assertNotNull(resultChain.getConfig(config1.getId()));
    assertEquals(config1.getId(), resultChain.getConfigs().iterator().next().getId());
    //
    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("chains", "17")
      .hasMany(ChainConfig.class, ImmutableList.of(config1))
      .hasMany(ChainBinding.class, ImmutableList.of())
      .belongsTo(Account.class, "25");
  }

  @Test
  public void update_addFirstEmbeddedEntity_invalidEntity() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    when(chainDAO.readOne(same(access), eq(BigInteger.valueOf(17))))
      .thenReturn(newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z")));
    PayloadObject updated = newChain(17, 25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print", Instant.parse("2014-08-12T12:17:02.527142Z")).toPayloadObject();
    ChainConfig badConfig = new ChainConfig()
      .setChainId(BigInteger.valueOf(17))
      .setTypeEnum(ChainConfigType.OutputChannels)
      .setValue("Not a (required) Numeric Value");
    badConfig.setId(UUID.randomUUID());
    updated.add("configs", new Payload().setDataMany(ImmutableList.of(badConfig.toPayloadObject())));
    Payload payload = new Payload()
      .setDataOne(updated)
      .addIncluded(badConfig.toPayloadObject());
    subject.id = "17";

    Response result = subject.update(payload, crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasErrorCount(1)
      .hasDataOne("chains", "17")
      .hasMany(ChainConfig.class, ImmutableList.of())
      .hasMany(ChainBinding.class, ImmutableList.of())
      .belongsTo(Account.class, "25");
  }

}
