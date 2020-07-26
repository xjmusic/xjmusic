// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.persistence;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.entity.Account;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.ChainState;
import io.xj.service.nexus.entity.ChainType;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentState;
import io.xj.service.nexus.entity.SegmentType;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class NexusEntityStoreImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private NexusEntityStore subject;
  private EntityFactory entityFactory;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new NexusEntityStoreModule()));
    entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Instantiate the test subject and put the payload
    subject = injector.getInstance(NexusEntityStore.class);
  }

  /**
   This should ostensibly be a test inside the Entity library-- and it is, except for this bug that
   at the time of this writing, we couldn't isolate to that library, and are therefore reproducing it here.

   @throws EntityException on failure
   */
  @Test
  public void internal_entityFactoryClonesSegmentTypeOK() throws EntityException {
    Segment segment = new Segment().setTypeEnum(SegmentType.NextMacro);

    Segment result = entityFactory.clone(segment);

    assertEquals(SegmentType.NextMacro, result.getType());
  }


  @Test
  public void put_get_Segment() throws NexusEntityStoreException {
    UUID chainId = UUID.randomUUID();
    Segment segment = Segment.create()
      .setChainId(chainId)
      .setOffset(0L)
      .setTypeEnum(SegmentType.NextMacro)
      .setStateEnum(SegmentState.Dubbed)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z")
      .setKey("D Major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav");

    subject.put(segment);
    Segment result = subject.get(Segment.class, segment.getId()).orElseThrow();

    assertEquals(segment.getId(), result.getId());
    assertEquals(chainId, result.getChainId());
    assertEquals(Long.valueOf(0L), result.getOffset());
    assertEquals(SegmentType.NextMacro, result.getType());
    assertEquals(SegmentState.Dubbed, result.getState());
    assertEquals(Instant.parse("2017-02-14T12:01:00.000001Z"), result.getBeginAt());
    assertEquals(Instant.parse("2017-02-14T12:01:32.000001Z"), result.getEndAt());
    assertEquals("D Major", result.getKey());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(Double.valueOf(0.73), result.getDensity());
    assertEquals(Double.valueOf(120.0), result.getTempo());
    assertEquals("chains-1-segments-9f7s89d8a7892.wav", result.getStorageKey());
  }

  @Test
  public void put_get_Chain() throws NexusEntityStoreException {
    UUID accountId = UUID.randomUUID();
    Chain chain = Chain.create()
      .setAccountId(accountId)
      .setTypeEnum(ChainType.Preview)
      .setStateEnum(ChainState.Fabricate)
      .setStartAt("2017-02-14T12:01:00.000001Z")
      .setStopAt("2017-02-14T12:01:32.000001Z")
      .setEmbedKey("super");

    subject.put(chain);
    Chain result = subject.get(Chain.class, chain.getId()).orElseThrow();

    assertEquals(chain.getId(), result.getId());
    assertEquals(accountId, result.getAccountId());
    assertEquals(ChainType.Preview, result.getType());
    assertEquals(ChainState.Fabricate, result.getState());
    assertEquals(Instant.parse("2017-02-14T12:01:00.000001Z"), result.getStartAt());
    assertEquals(Instant.parse("2017-02-14T12:01:32.000001Z"), result.getStopAt());
    assertEquals("super", result.getEmbedKey());
  }

  @Test
  public void put_cantBeMutated() throws NexusEntityStoreException {
    Account account1 = Account.create("fish");
    Chain chain3 = Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null);
    subject.put(chain3);
    // and then after putting the entity, change the object that was sent-- this should NOT mutate the store
    chain3.setName("FunkyTown");

    Chain result = subject.get(Chain.class, chain3.getId()).orElseThrow();
    assertEquals("Test Print #1", result.getName());
  }

  @Test
  public void get_cantBeMutated() throws NexusEntityStoreException {
    Account account1 = Account.create("fish");
    Chain chain3 = subject.put(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    Chain got = subject.get(Chain.class, chain3.getId()).orElseThrow();
    // and then after getting an entity, change the object we got-- this should NOT mutate the store
    got.setName("FunkyTown");

    Chain result = subject.get(Chain.class, chain3.getId()).orElseThrow();
    assertEquals("Test Print #1", result.getName());
  }

  @Test
  public void getAll_cantBeMutated() throws NexusEntityStoreException {
    Account account1 = Account.create("fish");
    Chain chain3 = subject.put(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    Collection<Chain> got = subject.getAll(Chain.class);
    // and then after getting the entities, change one of the objects-- this should NOT mutate the store
    got.iterator().next().setName("FunkyTown");

    Chain result = subject.get(Chain.class, chain3.getId()).orElseThrow();
    assertEquals("Test Print #1", result.getName());
  }

  @Test
  public void getAllBelongingTo_cantBeMutated() throws NexusEntityStoreException {
    Account account1 = Account.create("fish");
    Chain chain3 = subject.put(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null));
    Collection<Chain> got = subject.getAll(Chain.class, Account.class, ImmutableList.of(account1.getId()));
    // and then after getting the entities, change one of the objects-- this should NOT mutate the store
    got.iterator().next().setName("FunkyTown");

    Chain result = subject.get(Chain.class, chain3.getId()).orElseThrow();
    assertEquals("Test Print #1", result.getName());
  }

  @Test
  public void putWithoutId_getHasNewId() throws NexusEntityStoreException {
    Account account1 = Account.create("fish");
    Chain chain3 = Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null);
    chain3.setId(null);
    Segment chain3_segment0 = Segment.create()
      .setChainId(chain3.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z")
      .setKey("D Major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav");
    subject.put(chain3_segment0);

    Segment result = subject.get(Segment.class, chain3_segment0.getId()).orElseThrow();
    assertEquals(chain3_segment0.getId(), result.getId());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(Long.valueOf(0L), result.getOffset());
    assertEquals(SegmentState.Dubbed, result.getState());
    assertEquals(Instant.parse("2017-02-14T12:01:00.000001Z"), result.getBeginAt());
    assertEquals(Instant.parse("2017-02-14T12:01:32.000001Z"), result.getEndAt());
    assertEquals("D Major", result.getKey());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(Double.valueOf(0.73), result.getDensity());
    assertEquals(Double.valueOf(120.0), result.getTempo());
    assertEquals("chains-1-segments-9f7s89d8a7892.wav", result.getStorageKey());
  }

  @Test
  public void putAll_getAll() throws NexusEntityStoreException {
    Account account1 = Account.create("fish");
    Chain chain2 = Chain.create(account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null);
    Chain chain3 = Chain.create(account1, "Test Print #3", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-09-11T12:17:01.047563Z"), null);
    Segment chain2_segment0 = Segment.create()
      .setChainId(chain2.getId())
      .setOffset(12L)
      .setStateEnum(SegmentState.Dubbed)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z")
      .setKey("G minor")
      .setTotal(32)
      .setDensity(0.3)
      .setTempo(10.0)
      .setStorageKey("chains-2-segments-8929f7sd8a789.wav");
    Segment chain3_segment0 = Segment.create()
      .setChainId(chain3.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z")
      .setKey("D Major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setStorageKey("chains-3-segments-9f7s89d8a7892.wav");
    Segment chain3_segment1 = Segment.create()
      .setChainId(chain3.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setEndAt("2017-02-14T12:01:48.000001Z")
      .setKey("D Major")
      .setTotal(48)
      .setDensity(0.73)
      .setTempo(120.0)
      .setStorageKey("chains-3-segments-d8a78929f7s89.wav");
    assertEquals(5, subject.putAll(ImmutableList.of(chain2, chain3, chain2_segment0, chain3_segment0, chain3_segment1)).size());

    Collection<Segment> result = subject.getAll(Segment.class, Chain.class, ImmutableList.of(chain3.getId()));
    assertEquals(2, result.size());
  }

}
