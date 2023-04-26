// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.persistence;


import io.xj.hub.HubTopology;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.lib.entity.EntityException;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequence;
import static io.xj.hub.IntegrationTestingFixtures.buildProgramSequenceBinding;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplateBinding;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class NexusEntityStoreImplTest {
  private NexusEntityStore subject;
  private EntityFactory entityFactory;

  @Before
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Instantiate the test subject and put the payload
    subject = new NexusEntityStoreImpl(entityFactory);
  }

  /**
   * This should ostensibly be a test inside the Entity library-- and it is, except for this bug that
   * at the time of this writing, we couldn't isolate to that library, and are therefore reproducing it here.
   *
   * @throws EntityException on failure
   */
  @Test
  public void internal_entityFactoryClonesSegmentTypeOK() throws EntityException {
    Segment segment = new Segment();
    segment.setType(SegmentType.NEXTMACRO);

    Segment result = entityFactory.clone(segment);

    assertEquals(SegmentType.NEXTMACRO, result.getType());
  }


  @Test
  public void put_get_Segment() throws NexusException {
    UUID chainId = UUID.randomUUID();
    Segment segment = new Segment();
    segment.setId(UUID.randomUUID());
    segment.setChainId(chainId);
    segment.setOffset(0L);
    segment.setType(SegmentType.NEXTMACRO);
    segment.setState(SegmentState.DUBBED);
    segment.beginAt("2017-02-14T12:01:00.000001Z");
    segment.endAt("2017-02-14T12:01:32.000001Z");
    segment.setKey("D Major");
    segment.setTotal(64);
    segment.setDensity(0.73);
    segment.setTempo(120.0);
    segment.storageKey("chains-1-segments-9f7s89d8a7892.wav");

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
    var chain = new Chain();
    chain.setId(UUID.randomUUID());
    chain.setAccountId(accountId);
    chain.setType(ChainType.PREVIEW);
    chain.setState(ChainState.FABRICATE);
    chain.startAt("2017-02-14T12:01:00.000001Z");
    chain.stopAt("2017-02-14T12:01:32.000001Z");
    chain.shipKey("super");

    subject.put(chain);
    var result = subject.getChain(chain.getId()).orElseThrow();

    assertEquals(chain.getId(), result.getId());
    assertEquals(accountId, result.getAccountId());
    assertEquals(ChainType.PREVIEW, result.getType());
    assertEquals(ChainState.FABRICATE, result.getState());
    assertEquals("2017-02-14T12:01:00.000001Z", result.getStartAt());
    assertEquals("2017-02-14T12:01:32.000001Z", result.getStopAt());
    assertEquals("super", result.getShipKey());
  }

  @Test
  public void put() throws NexusException {
    var segment = new Segment();
    segment.setId(UUID.randomUUID());
    segment.setChainId(UUID.randomUUID());
    segment.setOffset(0L);
    segment.setState(SegmentState.DUBBED);
    segment.beginAt("2017-02-14T12:01:00.000001Z");
    segment.endAt("2017-02-14T12:01:32.000001Z");
    segment.setKey("D Major");
    segment.setTotal(64);
    segment.setDensity(0.73);
    segment.setTempo(120.0);
    segment.storageKey("chains-1-segments-9f7s89d8a7892.wav");
    subject.put(segment);
  }

  @Test
  public void put_passThroughIfNotNexusEntity() throws NexusException {
    var library = new Library();
    library.setId(UUID.randomUUID());
    library.setAccountId(UUID.randomUUID());
    library.setName("helm");

    var result = subject.put(library);

    assertEquals(library, result);
  }

  @Test
  public void put_failsWithoutId() {
    var seg = new Segment();
    seg.setChainId(UUID.randomUUID());
    seg.setOffset(0L);
    seg.setState(SegmentState.DUBBED);
    seg.beginAt("2017-02-14T12:01:00.000001Z");
    seg.endAt("2017-02-14T12:01:32.000001Z");
    seg.setKey("D Major");
    seg.setTotal(64);
    seg.setDensity(0.73);
    seg.setTempo(120.0);
    seg.storageKey("chains-1-segments-9f7s89d8a7892.wav");

    var failure = assertThrows(NexusException.class,
      () -> subject.put(seg));

    assertEquals("Can't store Segment with null id", failure.getMessage());
  }

  @Test
  public void put_subEntityFailsWithoutSegmentId() {
    var choice = new SegmentChoice();
    choice.setId(UUID.randomUUID());
    choice.setProgramId(UUID.randomUUID());
    choice.setDeltaIn(Segments.DELTA_UNLIMITED);
    choice.setDeltaOut(Segments.DELTA_UNLIMITED);
    choice.setProgramSequenceBindingId(UUID.randomUUID());
    choice.setProgramType(ProgramType.Macro.toString());

    var failure = assertThrows(NexusException.class,
      () -> subject.put(choice));

    assertEquals("Can't store SegmentChoice without Segment ID!", failure.getMessage());
  }

  @Test
  public void putAll_getAll() throws NexusException {
    var account1 = buildAccount("fish");
    var template = buildTemplate(account1, "fishy");
    var chain2 = subject.put(buildChain(
      account1,
      "Test Print #2",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      template,
      Instant.parse("2014-08-12T12:17:02.527142Z"),
      null,
      "key123"));
    var chain3 = subject.put(buildChain(
      account1,
      "Test Print #3",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      template,
      Instant.parse("2014-08-12T12:17:02.527142Z"),
      null,
      "key123"));
    var program = buildProgram(ProgramType.Macro, "C", 120.0f, 0.6f);
    var programSequence = buildProgramSequence(program, 8, "Hay", 0.6f, "G");
    var programSequenceBinding = buildProgramSequenceBinding(programSequence, 0);
    subject.put(buildSegment(chain2,
      12,
      SegmentState.DUBBED,
      Instant.parse("2017-02-14T12:01:00.000001Z"),
      Instant.parse("2017-02-14T12:01:32.000001Z"),
      "G minor",
      32,
      0.3,
      10.0,
      "chains-2-segments-8929f7sd8a789.wav",
      "OGG"));
    Segment chain3_segment0 = subject.put(buildSegment(chain3,
      0,
      SegmentState.DUBBED,
      Instant.parse("2017-02-14T12:01:00.000001Z"),
      Instant.parse("2017-02-14T12:01:32.000001Z"),
      "D Major",
      64,
      0.73,
      120.0,
      "chains-3-segments-9f7s89d8a7892.wav",
      "OGG"));
    subject.put(buildSegmentChoice(chain3_segment0, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, program, programSequenceBinding));
    // not in the above chain, won't be retrieved with it
    subject.put(buildSegment(chain3,
      0,
      SegmentState.DUBBED,
      Instant.parse("2017-02-14T12:01:32.000001Z"),
      Instant.parse("2017-02-14T12:01:48.000001Z"),
      "D Major",
      48,
      0.73,
      120.0,
      "chains-3-segments-d8a78929f7s89.wav",
      "OGG"));

    Collection<Segment> result = subject.getAllSegments(chain3.getId());
    assertEquals(2, result.size());
    Collection<SegmentChoice> resultChoices = subject.getAll(chain3_segment0.getId(), SegmentChoice.class);
    assertEquals(1, resultChoices.size());
  }

  @Test
  public void put_nonSegmentEntity() throws NexusException {
    Account account1 = buildAccount("testing");
    Library library1 = buildLibrary(account1, "leaves");
    Template template = buildTemplate(buildAccount("Test"), TemplateType.Preview, "Test", "key123");
    TemplateBinding templateBinding = buildTemplateBinding(template, library1);

    subject.put(templateBinding);
  }

}
