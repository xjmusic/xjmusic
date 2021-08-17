// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.persistence;

import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import io.xj.api.Account;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.ChainType;
import io.xj.api.Library;
import io.xj.api.ProgramType;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.Segments;
import io.xj.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class NexusEntityStoreImplTest {
  private NexusEntityStore subject;
  private EntityFactory entityFactory;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Environment env = Environment.getDefault();
    var injector = AppConfiguration.inject(config, env, ImmutableSet.of(new NexusEntityStoreModule()));
    entityFactory = injector.getInstance(EntityFactory.class);
    Topology.buildHubApiTopology(entityFactory);
    Topology.buildNexusApiTopology(entityFactory);

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
    Segment segment = new Segment().type(SegmentType.NEXTMACRO);

    Segment result = entityFactory.clone(segment);

    assertEquals(SegmentType.NEXTMACRO, result.getType());
  }


  @Test
  public void put_get_Segment() throws NexusException {
    UUID chainId = UUID.randomUUID();
    Segment segment = new Segment()
      .id(UUID.randomUUID())
      .chainId(chainId)
      .offset(0L)
      .type(SegmentType.NEXTMACRO)
      .state(SegmentState.DUBBED)
      .beginAt("2017-02-14T12:01:00.000001Z")
      .endAt("2017-02-14T12:01:32.000001Z")
      .key("D Major")
      .total(64)
      .density(0.73)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav");

    subject.put(segment);
    Segment result = subject.getSegment(segment.getId()).orElseThrow();

    assertEquals(segment.getId(), result.getId());
    assertEquals(chainId, result.getChainId());
    assertEquals(Long.valueOf(0), result.getOffset());
    assertEquals(SegmentType.NEXTMACRO, result.getType());
    assertEquals(SegmentState.DUBBED, result.getState());
    assertEquals("2017-02-14T12:01:00.000001Z", result.getBeginAt());
    assertEquals("2017-02-14T12:01:32.000001Z", result.getEndAt());
    assertEquals("D Major", result.getKey());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.73, result.getDensity(), 0.01);
    assertEquals(120.0, result.getTempo(), 0.01);
    assertEquals("chains-1-segments-9f7s89d8a7892.wav", result.getStorageKey());
  }

  @Test
  public void put_get_Chain() throws NexusException {
    UUID accountId = UUID.randomUUID();
    var chain = new Chain()
      .id(UUID.randomUUID())
      .accountId(accountId)
      .type(ChainType.PREVIEW)
      .state(ChainState.FABRICATE)
      .startAt("2017-02-14T12:01:00.000001Z")
      .stopAt("2017-02-14T12:01:32.000001Z")
      .embedKey("super");

    subject.put(chain);
    var result = subject.getChain(chain.getId()).orElseThrow();

    assertEquals(chain.getId(), result.getId());
    assertEquals(accountId, result.getAccountId());
    assertEquals(ChainType.PREVIEW, result.getType());
    assertEquals(ChainState.FABRICATE, result.getState());
    assertEquals("2017-02-14T12:01:00.000001Z", result.getStartAt());
    assertEquals("2017-02-14T12:01:32.000001Z", result.getStopAt());
    assertEquals("super", result.getEmbedKey());
  }

  @Test
  public void put() throws NexusException {
    subject.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(UUID.randomUUID())
      .offset(0L)
      .state(SegmentState.DUBBED)
      .beginAt("2017-02-14T12:01:00.000001Z")
      .endAt("2017-02-14T12:01:32.000001Z")
      .key("D Major")
      .total(64)
      .density(0.73)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      );
  }

  @Test
  public void put_failsIfNotNexusEntity() {
    var failure = assertThrows(NexusException.class,
      () -> subject.put(new Library()
        .id(UUID.randomUUID())
        .accountId(UUID.randomUUID())
        .name("helm")
        ));

    assertEquals("Can't store Library!", failure.getMessage());
  }

  @Test
  public void put_failsWithoutId() {
    var failure = assertThrows(NexusException.class,
      () -> subject.put(new Segment()
        .chainId(UUID.randomUUID())
        .offset(0L)
        .state(SegmentState.DUBBED)
        .beginAt("2017-02-14T12:01:00.000001Z")
        .endAt("2017-02-14T12:01:32.000001Z")
        .key("D Major")
        .total(64)
        .density(0.73)
        .tempo(120.0)
        .storageKey("chains-1-segments-9f7s89d8a7892.wav")
        ));

    assertEquals("Can't store Segment with null id", failure.getMessage());
  }

  @Test
  public void put_subEntityFailsWithoutSegmentId() {
    var failure = assertThrows(NexusException.class,
      () -> subject.put(new SegmentChoice()
        .id(UUID.randomUUID())
        .programId(UUID.randomUUID())
        .deltaIn(Segments.DELTA_UNLIMITED)
        .deltaOut(Segments.DELTA_UNLIMITED)
        .programSequenceBindingId(UUID.randomUUID())
        .programType(ProgramType.MACRO)
        ));

    assertEquals("Can't store SegmentChoice without Segment ID!", failure.getMessage());
  }

  @Test
  public void putAll_getAll() throws NexusException {
    var account1 = new Account()
      .id(UUID.randomUUID())
      .name("fish");
    var chain2 = subject.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("Test Print #2")
      .type(ChainType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2014-08-12T12:17:02.527142Z")
      .stopAt("2014-09-11T12:17:01.047563Z")
      );
    var chain3 = subject.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("Test Print #3")
      .type(ChainType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2014-08-12T12:17:02.527142Z")
      .stopAt("2014-09-11T12:17:01.047563Z")
      );
    subject.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain2.getId())
      .offset(12L)
      .state(SegmentState.DUBBED)
      .beginAt("2017-02-14T12:01:00.000001Z")
      .endAt("2017-02-14T12:01:32.000001Z")
      .key("G minor")
      .total(32)
      .density(0.3)
      .tempo(10.0)
      .storageKey("chains-2-segments-8929f7sd8a789.wav")
      );
    Segment chain3_segment0 = subject.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain3.getId())
      .offset(0L)
      .state(SegmentState.DUBBED)
      .beginAt("2017-02-14T12:01:00.000001Z")
      .endAt("2017-02-14T12:01:32.000001Z")
      .key("D Major")
      .total(64)
      .density(0.73)
      .tempo(120.0)
      .storageKey("chains-3-segments-9f7s89d8a7892.wav")     );
    subject.put(new SegmentChoice()
      .id(UUID.randomUUID())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .segmentId(chain3_segment0.getId())
      .programId(UUID.randomUUID())
      .programSequenceBindingId(UUID.randomUUID())
      .programType(ProgramType.MACRO)
      );
    // not in the above chain, won't be retrieved with it
    subject.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain3.getId())
      .offset(0L)
      .state(SegmentState.DUBBED)
      .beginAt("2017-02-14T12:01:32.000001Z")
      .endAt("2017-02-14T12:01:48.000001Z")
      .key("D Major")
      .total(48)
      .density(0.73)
      .tempo(120.0)
      .storageKey("chains-3-segments-d8a78929f7s89.wav")
      );

    Collection<Segment> result = subject.getAllSegments(chain3.getId());
    assertEquals(2, result.size());
    Collection<SegmentChoice> resultChoices = subject.getAll(chain3_segment0.getId(), SegmentChoice.class);
    assertEquals(1, resultChoices.size());
  }
}
