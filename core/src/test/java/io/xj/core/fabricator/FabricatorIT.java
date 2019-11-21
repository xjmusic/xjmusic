 //  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentState;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static io.xj.core.testing.Assert.assertExactMemes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class FabricatorIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private FabricatorFactory fabricatorFactory;
  private SegmentDAO segmentDAO;
  private SegmentChoice segmentChoice4_main;

  @Before
  public void setUp() throws CoreException {
    reset();
    insertFixtureB1();
    insertFixtureB2();

    // Chain "Test Print #1" has 5 total segments
    chain3 = insert(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    insert(ChainBinding.create(chain3, library2));

    segment1 = insert(new Segment().setId(UUID.randomUUID())
      .setChainId(chain3.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z"));
    segment2 = insert(new Segment().setId(UUID.randomUUID())
      .setChainId(chain3.getId())
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setEndAt("2017-02-14T12:02:04.000001Z"));

    segment3 = insert(new Segment().setId(UUID.randomUUID())
      .setChainId(chain3.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("Ab minor")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z"));

    segment4 = insert(new Segment().setId(UUID.randomUUID())
      .setChainId(chain3.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("F minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(125.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z"));

    segmentChoice4_main = insert(SegmentChoice.create(segment4, ProgramType.Main, program5_binding0, 0));

    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    segmentDAO = injector.getInstance(SegmentDAO.class);
  }

  /**
   [#165954619] Choice is by sequence, for rhythm- and detail-type sequences
   */
  @Test
  public void getSequenceOfChoice_bySequence() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segmentDAO.readOne(access, segment4.getId()));

    ProgramSequence result = fabricator.getSequence(new SegmentChoice()
      .setTypeEnum(ProgramType.Rhythm)
      .setProgramId(program5.getId()));

    assertNotNull(result);
  }

  /**
   [#165954619] Choice is by sequence-pattern, for macro- or main-type sequences
   */
  @Test
  public void getSequenceOfChoice_bySequenceBinding() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segmentDAO.readOne(access, segment4.getId()));

    ProgramSequence result = fabricator.getSequence(new SegmentChoice()
      .setProgramId(segmentChoice4_main.getProgramId())
      .setTypeEnum(ProgramType.Main));

    assertNotNull(result);
  }

  @Test
  public void getMemesOfChoice() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segmentDAO.readOne(access, segment4.getId()));
    Segment segment = Segment.create();

    assertExactMemes(Lists.newArrayList("Basic"), fabricator.getMemesOfChoice(SegmentChoice.create(segment, ProgramType.Rhythm, program35, 0)));
    assertExactMemes(Lists.newArrayList("Wild", "Tropical"), fabricator.getMemesOfChoice(SegmentChoice.create(segment, ProgramType.Macro, program4_binding0, 0)));
    assertExactMemes(Lists.newArrayList("Pessimism", "Outlook"), fabricator.getMemesOfChoice(SegmentChoice.create(segment, ProgramType.Main, program5_binding1, 0)));
    assertExactMemes(Lists.newArrayList("Chunky", "Tangy"), fabricator.getMemesOfChoice(SegmentChoice.create(segment, ProgramType.Main, program3_binding0, 0)));
    assertExactMemes(Lists.newArrayList("Regret", "Hindsight"), fabricator.getMemesOfChoice(SegmentChoice.create(segment, ProgramType.Main, program15_binding0, 0)));
    assertExactMemes(Lists.newArrayList("Pride", "Shame", "Hindsight"), fabricator.getMemesOfChoice(SegmentChoice.create(segment, ProgramType.Main, program15_binding1, 0)));
  }

  @Test
  public void getPreviousSegmentsWithSameMainProgram() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");
    segment5 = insert(new Segment()
      .setId(UUID.randomUUID())
      .setChainId(chain3.getId())
      .setOffset(4L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("F minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(125.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create(segment5, ProgramType.Main, program5_binding0, 0)); // choose the same main program as segment4
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segmentDAO.readOne(access, segment5.getId()));

    Collection<Segment> result = fabricator.getPreviousSegmentsWithSameMainProgram();

    assertEquals(1, result.size());
  }

  @Test
  public void getMaxAvailableSequenceBindingOffset() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");
    Program program25 = insert(Program.create(user3, library2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, 0.6));
    ProgramSequence sequence25a = insert(ProgramSequence.create(program25, 0, "Chunk", 0.4, "G minor", 115.0));
    insert(ProgramSequenceBinding.create(sequence25a, 0));
    UUID sequenceBindingId = insert(ProgramSequenceBinding.create(sequence25a, 1)).getId();
    insert(ProgramSequenceBinding.create(sequence25a, 2));
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segmentDAO.readOne(access, segment4.getId()));

    assertEquals(Long.valueOf(2L), fabricator.getMaxAvailableSequenceBindingOffset(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .setProgramSequenceBindingId(sequenceBindingId)));
  }

  @Test
  public void getNextSequenceBindingOffset() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");
    Program program25 = insert(Program.create(user3, library2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, 0.6));
    ProgramSequence sequence25a = insert(ProgramSequence.create(program25, 0, "Chunk", 0.4, "G minor", 115.0));
    UUID sequenceBindingId = insert(ProgramSequenceBinding.create(sequence25a, 0)).getId();
    insert(ProgramSequenceBinding.create(sequence25a, 1));
    insert(ProgramSequenceBinding.create(sequence25a, 2));
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segmentDAO.readOne(access, segment4.getId()));

    assertEquals(Long.valueOf(1L), fabricator.getNextSequenceBindingOffset(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .setProgramSequenceBindingId(sequenceBindingId)));
  }

  @Test
  public void getNextSequenceBindingOffset_endLoopsBackToZero() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");
    Program program25 = insert(Program.create(user3, library2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, 0.6));
    ProgramSequence sequence25a = insert(ProgramSequence.create(program25, 0, "Chunk", 0.4, "G minor", 115.0));
    insert(ProgramSequenceBinding.create(sequence25a, 0));
    insert(ProgramSequenceBinding.create(sequence25a, 1));
    UUID sequenceBindingId = insert(ProgramSequenceBinding.create(sequence25a, 2)).getId();
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segmentDAO.readOne(access, segment4.getId()));

    assertEquals(Long.valueOf(0L), fabricator.getNextSequenceBindingOffset(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .setProgramSequenceBindingId(sequenceBindingId)));
  }

  @Test
  public void hasOneMoreSequenceBindingOffset_true() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");
    Program program25 = insert(Program.create(user3, library2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, 0.6));
    ProgramSequence sequence25a = insert(ProgramSequence.create(program25, 0, "Chunk", 0.4, "G minor", 115.0));
    insert(ProgramSequenceBinding.create(sequence25a, 0));
    UUID sequenceBindingId = insert(ProgramSequenceBinding.create(sequence25a, 1)).getId();
    insert(ProgramSequenceBinding.create(sequence25a, 2));
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segmentDAO.readOne(access, segment4.getId()));

    assertTrue(fabricator.hasOneMoreSequenceBindingOffset(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .setProgramSequenceBindingId(sequenceBindingId)));
  }

  @Test
  public void hasOneMoreSequenceBindingOffset_false() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");
    Program program25 = insert(Program.create(user3, library2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, 0.6));
    ProgramSequence sequence25a = insert(ProgramSequence.create(program25, 0, "Chunk", 0.4, "G minor", 115.0));
    insert(ProgramSequenceBinding.create(sequence25a, 0));
    UUID sequenceBindingId = insert(ProgramSequenceBinding.create(sequence25a, 1)).getId();
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segmentDAO.readOne(access, segment4.getId()));

    assertFalse(fabricator.hasOneMoreSequenceBindingOffset(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .setProgramSequenceBindingId(sequenceBindingId)));
  }

  @Test
  public void hasTwoMoreSequenceBindingOffsets_true() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");
    Program program25 = insert(Program.create(user3, library2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, 0.6));
    ProgramSequence sequence25a = insert(ProgramSequence.create(program25, 0, "Chunk", 0.4, "G minor", 115.0));
    insert(ProgramSequenceBinding.create(sequence25a, 0));
    UUID sequenceBindingId = insert(ProgramSequenceBinding.create(sequence25a, 1)).getId();
    insert(ProgramSequenceBinding.create(sequence25a, 2));
    insert(ProgramSequenceBinding.create(sequence25a, 3));
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segmentDAO.readOne(access, segment4.getId()));

    assertTrue(fabricator.hasTwoMoreSequenceBindingOffsets(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .setProgramSequenceBindingId(sequenceBindingId)));
  }


  /**
   Same conditions as that return true for hasOneMore offset, can be false for hasTwoMore
   */
  @Test
  public void hasTwoMoreSequenceBindingOffsets_false() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "Artist");
    Program program25 = insert(Program.create(user3, library2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, 0.6));
    ProgramSequence sequence25a = insert(ProgramSequence.create(program25, 0, "Chunk", 0.4, "G minor", 115.0));
    insert(ProgramSequenceBinding.create(sequence25a, 0));
    UUID sequenceBindingId = insert(ProgramSequenceBinding.create(sequence25a, 1)).getId();
    insert(ProgramSequenceBinding.create(sequence25a, 2));
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segmentDAO.readOne(access, segment4.getId()));

    assertFalse(fabricator.hasTwoMoreSequenceBindingOffsets(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setSegmentId(UUID.randomUUID())
      .setProgramSequenceBindingId(sequenceBindingId)));
  }

}
