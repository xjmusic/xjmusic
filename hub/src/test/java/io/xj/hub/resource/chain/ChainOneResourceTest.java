//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.resource.chain;

import com.google.common.collect.ImmutableMap;
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

import static io.xj.core.access.Access.CONTEXT_KEY;
import static io.xj.core.testing.AssertPayload.assertPayload;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
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
    Account account25 = Account.create();
    Chain chain17 = Chain.create(account25, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");
    when(chainDAO.readOne(same(access), eq(chain17.getId()))).thenReturn(chain17);
    subject.id = chain17.getId().toString();

    Response result = subject.readOne(crc);

    assertEquals(200, result.getStatus());
    assertTrue(result.hasEntity());
    assertPayload(deserializePayload(result.getEntity()))
      .hasDataOne("chains", chain17.getId().toString());
  }


  /*

  FUTURE: implement these tests with ?include=entity,entity type parameter



  @Test
  public void readOne() throws CoreException, IOException {
    when(crc.getProperty(CONTEXT_KEY)).thenReturn(access);
    Chain chain17 = Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");
    ChainConfig config1 = chain17.add(ChainConfig.create(Chain.create(), ChainConfigType.OutputContainer, "AAC"));
    ChainConfig config2 = chain17.add(ChainConfig.create(Chain.create(), ChainConfigType.OutputChannels, "4"));
    ChainBinding binding1 = chain17.add(ChainBinding.create("Library", 5));
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
      .thenReturn(Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print"));
    Chain updated = Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");
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
      .thenReturn(Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print"));
    ChainConfig config1 = ChainConfig.create(Chain.create(), ChainConfigType.OutputContainer, "AAC");
    Chain updated = Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");
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
      .thenReturn(Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print"));
    Chain updated = Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");
    ChainConfig config1 = updated.add(ChainConfig.create(Chain.create(), ChainConfigType.OutputContainer, "aac"));
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
    Chain chainBefore = Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");
    ChainConfig config1 = chainBefore.add(ChainConfig.create(Chain.create(), ChainConfigType.OutputContainer, "AAC"));
    when(chainDAO.readOne(same(access), eq(BigInteger.valueOf(17))))
      .thenReturn(chainBefore);
    Chain chainAfter = Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");
    chainAfter.add(config1);
    ChainConfig config2 = chainAfter.add(ChainConfig.create(Chain.create(), ChainConfigType.OutputChannels, "4"));
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
    Chain chainBefore = Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");
    ChainConfig config1 = chainBefore.add(ChainConfig.create(Chain.create(), ChainConfigType.OutputContainer, "AAC"));
    chainBefore.add(ChainConfig.create(Chain.create(), ChainConfigType.OutputChannels, "4"));
    when(chainDAO.readOne(same(access), eq(BigInteger.valueOf(17))))
      .thenReturn(chainBefore);
    Chain chainAfter = Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print");
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
      .thenReturn(Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print"));
    PayloadObject updated = Chain.create(25, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "test_print").toPayloadObject();
    ChainConfig badConfig = new ChainConfig()
      .setChainId(chain1.getId())
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

   */
}
