// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.persistence;

import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import io.xj.Account;
import io.xj.Chain;
import io.xj.Library;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusException;
import io.xj.hub.HubApp;
import io.xj.nexus.NexusApp;
import io.xj.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

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
    var injector = AppConfiguration.inject(config, ImmutableSet.of(new NexusEntityStoreModule()));
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
    Segment segment = Segment.newBuilder().setType(Segment.Type.NextMacro).build();

    Segment result = entityFactory.clone(segment);

    assertEquals(Segment.Type.NextMacro, result.getType());
  }


  @Test
  public void put_get_Segment() throws NexusException {
    String chainId = UUID.randomUUID().toString();
    Segment segment = Segment.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setChainId(chainId)
            .setOffset(0L)
            .setType(Segment.Type.NextMacro)
            .setState(Segment.State.Dubbed)
            .setBeginAt("2017-02-14T12:01:00.000001Z")
            .setEndAt("2017-02-14T12:01:32.000001Z")
            .setKey("D Major")
            .setTotal(64)
            .setDensity(0.73)
            .setTempo(120.0)
            .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
            .build();

    subject.put(segment);
    Segment result = subject.getSegment(segment.getId()).orElseThrow();

    assertEquals(segment.getId(), result.getId());
    assertEquals(chainId, result.getChainId());
    assertEquals(0, result.getOffset());
    assertEquals(Segment.Type.NextMacro, result.getType());
    assertEquals(Segment.State.Dubbed, result.getState());
    assertEquals("2017-02-14T12:01:00.000001Z", result.getBeginAt());
    assertEquals("2017-02-14T12:01:32.000001Z", result.getEndAt());
    assertEquals("D Major", result.getKey());
    assertEquals(64, result.getTotal());
    assertEquals(0.73, result.getDensity(), 0.01);
    assertEquals(120.0, result.getTempo(), 0.01);
    assertEquals("chains-1-segments-9f7s89d8a7892.wav", result.getStorageKey());
  }

  @Test
  public void put_get_Chain() throws NexusException {
    String accountId = UUID.randomUUID().toString();
    var chain = Chain.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setAccountId(accountId)
            .setType(Chain.Type.Preview)
            .setState(Chain.State.Fabricate)
            .setStartAt("2017-02-14T12:01:00.000001Z")
            .setStopAt("2017-02-14T12:01:32.000001Z")
            .setEmbedKey("super")
            .build();

    subject.put(chain);
    var result = subject.getChain(chain.getId()).orElseThrow();

    assertEquals(chain.getId(), result.getId());
    assertEquals(accountId, result.getAccountId());
    assertEquals(Chain.Type.Preview, result.getType());
    assertEquals(Chain.State.Fabricate, result.getState());
    assertEquals("2017-02-14T12:01:00.000001Z", result.getStartAt());
    assertEquals("2017-02-14T12:01:32.000001Z", result.getStopAt());
    assertEquals("super", result.getEmbedKey());
  }

  @Test
  public void put() throws NexusException {
    subject.put(Segment.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setChainId(UUID.randomUUID().toString())
            .setOffset(0L)
            .setState(Segment.State.Dubbed)
            .setBeginAt("2017-02-14T12:01:00.000001Z")
            .setEndAt("2017-02-14T12:01:32.000001Z")
            .setKey("D Major")
            .setTotal(64)
            .setDensity(0.73)
            .setTempo(120.0)
            .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
            .build());
  }

  @Test
  public void put_failsIfNotNexusEntity() throws NexusException {
    failure.expect(NexusException.class);
    failure.expectMessage("Can't store Library");

    subject.put(Library.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setAccountId(UUID.randomUUID().toString())
            .setName("helm")
            .build());
  }

  @Test
  public void put_failsWithoutId() throws NexusException {
    failure.expect(NexusException.class);
    failure.expectMessage("Can't store Segment with null id");

    subject.put(Segment.newBuilder()
            .setChainId(UUID.randomUUID().toString())
            .setOffset(0L)
            .setState(Segment.State.Dubbed)
            .setBeginAt("2017-02-14T12:01:00.000001Z")
            .setEndAt("2017-02-14T12:01:32.000001Z")
            .setKey("D Major")
            .setTotal(64)
            .setDensity(0.73)
            .setTempo(120.0)
            .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
            .build());
  }

  @Test
  public void put_subEntityFailsWithoutSegmentId() throws NexusException {
    failure.expect(NexusException.class);
    failure.expectMessage("Can't store SegmentChoice without Segment ID");

    subject.put(SegmentChoice.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setProgramId(UUID.randomUUID().toString())
            .setProgramSequenceBindingId(UUID.randomUUID().toString())
            .setProgramType(Program.Type.Macro)
                        .build());
  }

  @Test
  public void putAll_getAll() throws NexusException {
    var account1 = Account.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setName("fish")
            .build();
    var chain2 = subject.put(Chain.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setAccountId(account1.getId())
            .setName("Test Print #2")
            .setType(Chain.Type.Production)
            .setState(Chain.State.Fabricate)
            .setStartAt("2014-08-12T12:17:02.527142Z")
            .setStopAt("2014-09-11T12:17:01.047563Z")
            .build());
    var chain3 = subject.put(Chain.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setAccountId(account1.getId())
            .setName("Test Print #3")
            .setType(Chain.Type.Production)
            .setState(Chain.State.Fabricate)
            .setStartAt("2014-08-12T12:17:02.527142Z")
            .setStopAt("2014-09-11T12:17:01.047563Z")
            .build());
    Segment chain2_segment0 = subject.put(Segment.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setChainId(chain2.getId())
            .setOffset(12L)
            .setState(Segment.State.Dubbed)
            .setBeginAt("2017-02-14T12:01:00.000001Z")
            .setEndAt("2017-02-14T12:01:32.000001Z")
            .setKey("G minor")
            .setTotal(32)
            .setDensity(0.3)
            .setTempo(10.0)
            .setStorageKey("chains-2-segments-8929f7sd8a789.wav")
            .build());
    Segment chain3_segment0 = subject.put(Segment.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setChainId(chain3.getId())
            .setOffset(0L)
            .setState(Segment.State.Dubbed)
            .setBeginAt("2017-02-14T12:01:00.000001Z")
            .setEndAt("2017-02-14T12:01:32.000001Z")
            .setKey("D Major")
            .setTotal(64)
            .setDensity(0.73)
            .setTempo(120.0)
            .setStorageKey("chains-3-segments-9f7s89d8a7892.wav")
            .build());
    subject.put(SegmentChoice.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSegmentId(chain3_segment0.getId())
            .setProgramId(UUID.randomUUID().toString())
            .setProgramSequenceBindingId(UUID.randomUUID().toString())
            .setProgramType(Program.Type.Macro)
                        .build());
    // not in the above chain, won't be retrieved with it
    subject.put(Segment.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setChainId(chain3.getId())
            .setOffset(0L)
            .setState(Segment.State.Dubbed)
            .setBeginAt("2017-02-14T12:01:32.000001Z")
            .setEndAt("2017-02-14T12:01:48.000001Z")
            .setKey("D Major")
            .setTotal(48)
            .setDensity(0.73)
            .setTempo(120.0)
            .setStorageKey("chains-3-segments-d8a78929f7s89.wav")
            .build());

    Collection<Segment> result = subject.getAllSegments(chain3.getId());
    assertEquals(2, result.size());
    Collection<SegmentChoice> resultChoices = subject.getAll(chain3_segment0.getId(), SegmentChoice.class);
    assertEquals(1, resultChoices.size());
  }
}
