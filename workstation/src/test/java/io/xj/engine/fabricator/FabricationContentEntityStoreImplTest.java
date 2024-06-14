// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.fabricator;


import io.xj.engine.ContentFixtures;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationException;
import io.xj.engine.FabricationTopology;
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactory;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.enums.ProgramType;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.pojos.Library;
import io.xj.model.pojos.Project;
import io.xj.model.pojos.Template;
import io.xj.model.pojos.TemplateBinding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

import static io.xj.engine.SegmentFixtures.buildChain;
import static io.xj.engine.SegmentFixtures.buildSegmentChoice;
import static io.xj.model.util.ValueUtils.MICROS_PER_SECOND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class FabricationContentEntityStoreImplTest {
  SegmentEntityStore subject;
  EntityFactory entityFactory;
  private Chain fakeChain;

  Chain chain3;
  Project project1;
  Segment segment1;
  Segment segment2;
  Segment segment4;
  Segment segment5;
  Template template1;

  @BeforeEach
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    FabricationTopology.buildFabricationTopology(entityFactory);

    // Instantiate the test subject and put the payload
    subject = new SegmentEntityStoreImpl(entityFactory);

    // add base fixtures
    Project fakeProject = ContentFixtures.buildProject("fake");
    fakeChain = buildChain(
      fakeProject,
      "Print #2",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      ContentFixtures.buildTemplate(fakeProject, "Test")
    );
    subject.put(fakeChain);
    project1 = ContentFixtures.buildProject("Testing");
    template1 = ContentFixtures.buildTemplate(project1, "Test Template 1", "test1");

    chain3 = subject.put(new Chain()
      .id(UUID.randomUUID())
      .projectId(project1.getId())
      .name("Test Print #1")
      .type(ChainType.PRODUCTION)
      .state(ChainState.FABRICATE));

    // Chain "Test Print #1" has 5 sequential segments
    segment1 = subject.put(new Segment()
      .id(0)
      .chainId(chain3.getId())
      .delta(0)
      .type(SegmentType.INITIAL)
      .state(SegmentState.CRAFTED)
      .key("D major")
      .total(64)
      .intensity(0.73)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .beginAtChainMicros(0L)
      .durationMicros(32 * MICROS_PER_SECOND));
    segment2 = subject.put(new Segment()
      .id(1)
      .chainId(chain3.getId())
      .delta(64)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTING)
      .key("Db minor")
      .total(64)
      .intensity(0.85)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .beginAtChainMicros(32 * MICROS_PER_SECOND)
      .waveformPreroll(1.523)
      .durationMicros(32 * MICROS_PER_SECOND));
    subject.put(new Segment()
      .id(2)
      .chainId(chain3.getId())
      .delta(256)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTED)
      .key("F major")
      .total(64)
      .intensity(0.30)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .beginAtChainMicros(2 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND));
    segment4 = subject.put(new Segment()
      .id(3)
      .chainId(chain3.getId())
      .state(SegmentState.CRAFTING)
      .key("E minor")
      .total(64)
      .delta(192)
      .type(SegmentType.CONTINUE)
      .intensity(0.41)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .beginAtChainMicros(3 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND));
    segment5 = subject.put(new Segment()
      .id(4)
      .chainId(chain3.getId())
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .delta(245)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.PLANNED)
      .key("E minor")
      .total(64)
      .intensity(0.41)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892"));
  }

  /**
   Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation https://github.com/xjmusic/xjmusic/issues/301
   */
  @Test
  public void create() throws Exception {
    Segment inputData = new Segment()
      .id(5)
      .chainId(chain3.getId())
      .state(SegmentState.PLANNED)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .beginAtChainMicros(5 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
      .total(64)
      .intensity(0.74)
      .waveformPreroll(2.898)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .key("C# minor 7 b9")
      .tempo(120.0);

    Segment result = subject.put(inputData);

    assertNotNull(result);
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(5, result.getId());
    assertEquals(SegmentState.PLANNED, result.getState());
    assertEquals(5 * 32 * MICROS_PER_SECOND, (long) result.getBeginAtChainMicros());
    assertEquals(32 * MICROS_PER_SECOND, (long) Objects.requireNonNull(result.getDurationMicros()));
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getIntensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0f, result.getTempo(), 0.01);
    assertEquals(2.898, result.getWaveformPreroll(), 0.01);
    assertNotNull(result.getStorageKey());
  }

  @Test
  public void create_get_Segment() throws FabricationException {
    UUID chainId = UUID.randomUUID();
    Segment segment = new Segment();
    segment.setChainId(chainId);
    segment.setId(0);
    segment.setType(SegmentType.NEXT_MACRO);
    segment.setState(SegmentState.CRAFTED);
    segment.beginAtChainMicros(0L);
    segment.durationMicros(32 * MICROS_PER_SECOND);
    segment.setKey("D Major");
    segment.setTotal(64);
    segment.setIntensity(0.73);
    segment.setTempo(120.0);
    segment.storageKey("chains-1-segments-9f7s89d8a7892.wav");

    subject.put(segment);
    Segment result = subject.readSegment(segment.getId()).orElseThrow();

    assertEquals(segment.getId(), result.getId());
    assertEquals(chainId, result.getChainId());
    assertEquals(0, result.getId());
    assertEquals(SegmentType.NEXT_MACRO, result.getType());
    assertEquals(SegmentState.CRAFTED, result.getState());
    assertEquals(0, (long) result.getBeginAtChainMicros());
    assertEquals(32 * MICROS_PER_SECOND, (long) Objects.requireNonNull(result.getDurationMicros()));
    assertEquals("D Major", result.getKey());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.73f, result.getIntensity(), 0.01);
    assertEquals(120.0f, result.getTempo(), 0.01);
    assertEquals("chains-1-segments-9f7s89d8a7892.wav", result.getStorageKey());
  }

  @Test
  public void create_get_Chain() throws FabricationException {
    UUID projectId = UUID.randomUUID();
    var chain = new Chain();
    chain.setId(UUID.randomUUID());
    chain.setProjectId(projectId);
    chain.setType(ChainType.PREVIEW);
    chain.setState(ChainState.FABRICATE);
    chain.shipKey("super");

    subject.put(chain);
    var result = subject.readChain().orElseThrow();

    assertEquals(chain.getId(), result.getId());
    assertEquals(projectId, result.getProjectId());
    assertEquals(ChainType.PREVIEW, result.getType());
    assertEquals(ChainState.FABRICATE, result.getState());
    assertEquals("super", result.getShipKey());
  }

  @Test
  public void create_passThroughIfNotFabricationEntity() throws FabricationException {
    var library = new Library();
    library.setId(UUID.randomUUID());
    library.setProjectId(UUID.randomUUID());
    library.setName("helm");

    var result = subject.put(library);

    assertEquals(library, result);
  }

  @Test
  public void create_failsWithoutId() {
    var choice = new SegmentChoice();
    choice.setProgramId(UUID.randomUUID());
    choice.setDeltaIn(Segment.DELTA_UNLIMITED);
    choice.setDeltaOut(Segment.DELTA_UNLIMITED);
    choice.setProgramSequenceBindingId(UUID.randomUUID());
    choice.setProgramType(ProgramType.Macro);

    var failure = assertThrows(FabricationException.class,
      () -> subject.put(choice));

    assertEquals("Can't store SegmentChoice with null id", failure.getMessage());
  }

  @Test
  public void create_subEntityFailsWithoutSegmentId() {
    var choice = new SegmentChoice();
    choice.setId(UUID.randomUUID());
    choice.setProgramId(UUID.randomUUID());
    choice.setDeltaIn(Segment.DELTA_UNLIMITED);
    choice.setDeltaOut(Segment.DELTA_UNLIMITED);
    choice.setProgramSequenceBindingId(UUID.randomUUID());
    choice.setProgramType(ProgramType.Macro);

    var failure = assertThrows(FabricationException.class,
      () -> subject.put(choice));

    assertEquals("Can't store SegmentChoice without Segment ID!", failure.getMessage());
  }

  @Test
  public void createAll_readAll() throws FabricationException {
    subject.clear();
    var project1 = ContentFixtures.buildProject("fish");
    var template = ContentFixtures.buildTemplate(project1, "fishy");
    var chain3 = subject.put(SegmentFixtures.buildChain(
      project1,
      "Test Print #3",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      template,
      "key123"));
    var program = ContentFixtures.buildProgram(ProgramType.Macro, "C", 120.0f);
    var programSequence = ContentFixtures.buildProgramSequence(program, 8, "Hay", 0.6f, "G");
    var programSequenceBinding = ContentFixtures.buildProgramSequenceBinding(programSequence, 0);
    Segment chain3_segment0 = subject.put(SegmentFixtures.buildSegment(chain3,
      0,
      SegmentState.CRAFTED,
      "D Major",
      64,
      0.73f,
      120.0f,
      "chains-3-segments-9f7s89d8a7892.wav"
    ));
    subject.put(buildSegmentChoice(chain3_segment0, Segment.DELTA_UNLIMITED, Segment.DELTA_UNLIMITED, program, programSequenceBinding));
    // not in the above chain, won't be retrieved with it
    subject.put(SegmentFixtures.buildSegment(chain3,
      1,
      SegmentState.CRAFTED,
      "D Major",
      48,
      0.73f,
      120.0f,
      "chains-3-segments-d8a78929f7s89.wav"
    ));

    Collection<Segment> result = subject.readAllSegments();
    assertEquals(2, result.size());
    Collection<SegmentChoice> resultChoices = subject.readAll(chain3_segment0.getId(), SegmentChoice.class);
    assertEquals(1, resultChoices.size());
  }

  @Test
  public void create_nonSegmentEntity() throws FabricationException {
    Project project1 = ContentFixtures.buildProject("testing");
    Library library1 = ContentFixtures.buildLibrary(project1, "leaves");
    Template template = ContentFixtures.buildTemplate(ContentFixtures.buildProject("Test"), "Test", "key123");
    TemplateBinding templateBinding = ContentFixtures.buildTemplateBinding(template, library1);

    subject.put(templateBinding);
  }

  @Test
  public void readSegment() throws Exception {
    Segment result = subject.readSegment(segment2.getId()).orElseThrow();

    assertNotNull(result);
    assertEquals(segment2.getId(), result.getId());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(1, result.getId());
    assertEquals(SegmentState.CRAFTING, result.getState());
    assertEquals(32 * MICROS_PER_SECOND, (long) result.getBeginAtChainMicros());
    assertEquals(32 * MICROS_PER_SECOND, (long) Objects.requireNonNull(result.getDurationMicros()));
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.85f, result.getIntensity(), 0.01);
    assertEquals("Db minor", result.getKey());
    assertEquals(120.0f, result.getTempo(), 0.01);
    assertEquals(1.523, result.getWaveformPreroll(), 0.01);
  }

  @Test
  public void readLastSegmentId() throws FabricationException {
    subject.put(SegmentFixtures.buildSegment(fakeChain,
      4,
      SegmentState.CRAFTED,
      "D Major",
      64,
      0.73f,
      120.0f,
      "chains-3-segments-9f7s89d8a7892.wav"
    ));

    assertEquals(4, subject.readLastSegmentId());
  }

  @Test
  public void readSegmentsFromToOffset() {
    Collection<Segment> result = subject.readSegmentsFromToOffset(2, 3);

    assertEquals(2L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment result1 = it.next();
    assertEquals(SegmentState.CRAFTED, result1.getState());
    Segment result2 = it.next();
    assertEquals(SegmentState.CRAFTING, result2.getState());
  }

  @Test
  public void readSegmentsFromToOffset_acceptsNegativeOffsets_returnsEmptyCollection() {
    Collection<Segment> result = subject.readSegmentsFromToOffset(-1, -1);

    assertEquals(0L, result.size());
  }

  @Test
  public void readSegmentsFromToOffset_trimsIfEndOffsetOutOfBounds() {
    Collection<Segment> result = subject.readSegmentsFromToOffset(2, 12);

    assertEquals(3L, result.size());
  }

  @Test
  public void readSegmentsFromToOffset_onlyOneIfEndOffsetSameAsStart() {
    Collection<Segment> result = subject.readSegmentsFromToOffset(2, 2);

    assertEquals(1L, result.size());
  }

  @Test
  public void readSegmentsFromToOffset_emptyIfStartOffsetOutOfBounds() {
    Collection<Segment> result = subject.readSegmentsFromToOffset(14, 17);

    assertEquals(0, result.size());
  }

  @Test
  public void readAllSegments() throws FabricationException {
    Collection<Segment> result = subject.readAllSegments();

    assertNotNull(result);
    assertEquals(5L, result.size());
    Iterator<Segment> it = result.iterator();

    Segment result0 = it.next();
    assertEquals(SegmentState.CRAFTED, result0.getState());

    Segment result1 = it.next();
    assertEquals(SegmentState.CRAFTING, result1.getState());

    Segment result2 = it.next();
    assertEquals(SegmentState.CRAFTED, result2.getState());

    Segment result3 = it.next();
    assertEquals(SegmentState.CRAFTING, result3.getState());

    Segment result4 = it.next();
    assertEquals(SegmentState.PLANNED, result4.getState());
  }

  /**
   List of Segments returned should not be more than a dozen or so https://github.com/xjmusic/xjmusic/issues/302
   */
  @Test
  public void readAll_hasNoLimit() throws FabricationException {
    Chain chain5 = subject.put(SegmentFixtures.buildChain(project1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, template1, "barnacles"));
    for (int i = 0; i < 20; i++)
      subject.put(new Segment()
        .chainId(chain5.getId())
        .id(i)
        .state(SegmentState.CRAFTING)
        .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
        .durationMicros(32 * MICROS_PER_SECOND)
        .total(64)
        .intensity(0.74)
        .key("C# minor 7 b9")
        .tempo(120.0));

    Collection<Segment> result = subject.readAllSegments();

    assertNotNull(result);
    assertEquals(20L, result.size());
  }

  @Test
  public void updateSegment() throws Exception {
    Segment inputData = new Segment()
      .id(1)
      .chainId(chain3.getId())
      .state(SegmentState.CRAFTED)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .total(64)
      .intensity(0.74)
      .waveformPreroll(0.0123)
      .key("C# minor 7 b9")
      .tempo(120.0);

    subject.updateSegment(inputData);

    Segment result = subject.readSegment(segment2.getId()).orElseThrow();
    assertNotNull(result);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(SegmentState.CRAFTED, result.getState());
    assertEquals(0.0123, result.getWaveformPreroll(), 0.001);
    assertEquals(4 * 32 * MICROS_PER_SECOND, (long) result.getBeginAtChainMicros());
    assertEquals(32 * MICROS_PER_SECOND, (long) Objects.requireNonNull(result.getDurationMicros()));
  }

  /**
   persist Segment content, then read prior Segment content
   */
  @Test
  public void updateSegment_persistPriorSegmentContent() throws Exception {
    segment4 = subject.put(new Segment()
      .id(5)
      .type(SegmentType.CONTINUE)
      .delta(0)
      .chainId(chain3.getId())
      .state(SegmentState.CRAFTED)
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
      .total(64)
      .intensity(0.74)
      .key("C# minor 7 b9")
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .tempo(120.0));

    subject.updateSegment(segment4);

    Segment result = subject.readSegment(segment2.getId()).orElseThrow();
    assertNotNull(result);
  }

  @Test
  public void updateSegment_failsToTransitionFromDubbingToCrafting() {
    Segment inputData = new Segment()
      .id(4)
      .chainId(segment5.getChainId())
      .state(SegmentState.CRAFTED)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
      .total(64)
      .intensity(0.74)
      .key("C# minor 7 b9")
      .tempo(120.0);

    Exception thrown = assertThrows(FabricationException.class, () ->
      subject.updateSegment(inputData));

    assertTrue(thrown.getMessage().contains("transition to Crafted not in allowed"));
  }

  @Test
  public void updateSegment_FailsToChangeChain() {
    Segment inputData = new Segment()
      .id(4)
      .chainId(UUID.randomUUID())
      .delta(0)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTING)
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
      .total(64)
      .intensity(0.74)
      .key("C# minor 7 b9")
      .tempo(120.0);

    Exception thrown = assertThrows(FabricationException.class, () ->
      subject.updateSegment(inputData));

    assertTrue(thrown.getMessage().contains("cannot change chainId create a segment"));
    Segment result = subject.readSegment(segment2.getId()).orElseThrow();
    assertNotNull(result);
    assertEquals("Db minor", result.getKey());
    assertEquals(chain3.getId(), result.getChainId());
  }

  @Test
  public void getSegmentCount() {
    Integer result = subject.getSegmentCount();

    assertEquals(5, result);
  }

  @Test
  public void isSegmentsEmpty() throws FabricationException {
    subject.clear();
    assertTrue(subject.isEmpty());

    subject.put(SegmentFixtures.buildSegment(fakeChain,
      0,
      SegmentState.CRAFTED,
      "D Major",
      64,
      0.73f,
      120.0f,
      "chains-3-segments-9f7s89d8a7892.wav"
    ));

    assertFalse(subject.isEmpty());
  }

  @Test
  public void deleteSegment() throws FabricationException {
    for (int i = 0; i < 10; i++)
      subject.put(SegmentFixtures.buildSegment(fakeChain,
        i,
        SegmentState.CRAFTED,
        "D Major",
        64,
        0.73f,
        120.0f,
        "chains-3-segments-9f7s89d8a7892.wav"
      ));

    subject.deleteSegment(5);

    assertEquals(9, subject.getSegmentCount());
    assertFalse(subject.readSegment(5).isPresent());
  }

  @Test
  public void deleteSegmentsAfter() throws FabricationException {
    for (int i = 0; i < 10; i++)
      subject.put(SegmentFixtures.buildSegment(fakeChain,
        i,
        SegmentState.CRAFTED,
        "D Major",
        64,
        0.73f,
        120.0f,
        "chains-3-segments-9f7s89d8a7892.wav"
      ));

    subject.deleteSegmentsAfter(5);

    assertEquals(6, subject.getSegmentCount());
    assertTrue(subject.readSegment(0).isPresent());
    assertTrue(subject.readSegment(1).isPresent());
    assertTrue(subject.readSegment(2).isPresent());
    assertTrue(subject.readSegment(3).isPresent());
    assertTrue(subject.readSegment(4).isPresent());
    assertTrue(subject.readSegment(5).isPresent());
    assertFalse(subject.readSegment(6).isPresent());
    assertFalse(subject.readSegment(7).isPresent());
    assertFalse(subject.readSegment(8).isPresent());
    assertFalse(subject.readSegment(9).isPresent());
  }

  @Test
  public void deleteSegmentsBefore() throws FabricationException {
    for (int i = 0; i < 10; i++)
      subject.put(SegmentFixtures.buildSegment(fakeChain,
        i,
        SegmentState.CRAFTED,
        "D Major",
        64,
        0.73f,
        120.0f,
        "chains-3-segments-9f7s89d8a7892.wav"
      ));

    subject.deleteSegmentsBefore(5);

    assertEquals(5, subject.getSegmentCount());
    assertFalse(subject.readSegment(0).isPresent());
    assertFalse(subject.readSegment(1).isPresent());
    assertFalse(subject.readSegment(2).isPresent());
    assertFalse(subject.readSegment(3).isPresent());
    assertFalse(subject.readSegment(4).isPresent());
    assertTrue(subject.readSegment(5).isPresent());
    assertTrue(subject.readSegment(6).isPresent());
    assertTrue(subject.readSegment(7).isPresent());
    assertTrue(subject.readSegment(8).isPresent());
    assertTrue(subject.readSegment(9).isPresent());
  }

}
