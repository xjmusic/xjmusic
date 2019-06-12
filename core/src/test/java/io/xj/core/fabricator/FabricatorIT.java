//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment.sub.Choice;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
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

  @Before
  public void setUp() throws CoreException {
    reset();
    insertFixtureB1();
    insertFixtureB2();

    // Chain "Test Print #1" has 5 total segments
    Chain chain = newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, now());
    chain.add(newChainBinding("Library", 2));
    insert(chain);
    insert(segmentFactory.newSegment(BigInteger.valueOf(1))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(2))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setEndAt("2017-02-14T12:02:04.000001Z"));

    insert(segmentFactory.newSegment(BigInteger.valueOf(3))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("Ab minor")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z"));

    segment4 = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("F minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(125.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z");
    segment4.add(newChoice(UUID.randomUUID(), ProgramType.Main, 5, injector.getInstance(ProgramDAO.class).readOne(Access.internal(), BigInteger.valueOf(5)).getSequenceBindingsAtOffset(0L).iterator().next().getId(), 0));
    insert(segment4);

    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    segmentDAO = injector.getInstance(SegmentDAO.class);
  }

  /**
   [#165954619] Choice is by sequence, for rhythm- and detail-type sequences
   */
  @Test
  public void getSequenceOfChoice_bySequence() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    Sequence result = fabricator.getSequence(new Choice()
      .setTypeEnum(ProgramType.Rhythm)
      .setProgramId(BigInteger.valueOf(35)));

    assertNotNull(result);
  }

  /**
   [#165954619] Choice is by sequence-pattern, for macro- or main-type sequences
   */
  @Test
  public void getSequenceOfChoice_bySequenceBinding() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));
    Program program = injector.getInstance(ProgramDAO.class).readOne(access, BigInteger.valueOf(4));

    Sequence result = fabricator.getSequence(new Choice()
      .setProgramId(BigInteger.valueOf(4))
      .setTypeEnum(ProgramType.Main)
      .setSequenceBindingId(program.getSequenceBindings().iterator().next().getId()));

    assertNotNull(result);
  }

  @Test
  public void getMemesOfChoice() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertExactMemes(Lists.newArrayList("Wild", "Tropical"), fabricator.getMemesOfChoice(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setProgramId(BigInteger.valueOf(4))
      .setSequenceBindingId(injector.getInstance(ProgramDAO.class).readOne(access, BigInteger.valueOf(4)).getSequenceBindingsAtOffset(0L).iterator().next().getId())
      .setTypeEnum(ProgramType.Macro)));
    assertExactMemes(Lists.newArrayList("Pessimism", "Outlook"), fabricator.getMemesOfChoice(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setProgramId(BigInteger.valueOf(5))
      .setSequenceBindingId(injector.getInstance(ProgramDAO.class).readOne(access, BigInteger.valueOf(5)).getSequenceBindingsAtOffset(1L).iterator().next().getId())
      .setTypeEnum(ProgramType.Main)));
    assertExactMemes(Lists.newArrayList("Basic"), fabricator.getMemesOfChoice(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setProgramId(BigInteger.valueOf(35))
      .setTypeEnum(ProgramType.Rhythm)));
    assertExactMemes(Lists.newArrayList("Chunky", "Tangy"), fabricator.getMemesOfChoice(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setProgramId(BigInteger.valueOf(3))
      .setSequenceBindingId(injector.getInstance(ProgramDAO.class).readOne(access, BigInteger.valueOf(3)).getSequenceBindingsAtOffset(0L).iterator().next().getId())
      .setTypeEnum(ProgramType.Main)));
    assertExactMemes(Lists.newArrayList("Regret", "Hindsight"), fabricator.getMemesOfChoice(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setProgramId(BigInteger.valueOf(15))
      .setSequenceBindingId(injector.getInstance(ProgramDAO.class).readOne(access, BigInteger.valueOf(15)).getSequenceBindingsAtOffset(0L).iterator().next().getId())
      .setTypeEnum(ProgramType.Main)));
    assertExactMemes(Lists.newArrayList("Pride", "Shame", "Hindsight"), fabricator.getMemesOfChoice(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setProgramId(BigInteger.valueOf(15))
      .setSequenceBindingId(injector.getInstance(ProgramDAO.class).readOne(access, BigInteger.valueOf(15)).getSequenceBindingsAtOffset(1L).iterator().next().getId())
      .setTypeEnum(ProgramType.Main)));
  }

  @Test
  public void getPreviousSegmentsWithSameMainProgram() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    // segment-1 at offset-0 of chain-1
    segment1 = segmentFactory.newSegment(BigInteger.valueOf(5))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(4L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("F minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(125.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav");
    segment1.add(new Choice()
      .setProgramId(BigInteger.valueOf(35))
      .setSequenceBindingId(UUID.randomUUID())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(4));
    segment1.add(newChoice(UUID.randomUUID(), ProgramType.Main, 5, injector.getInstance(ProgramDAO.class).readOne(Access.internal(), BigInteger.valueOf(5)).getSequenceBindingsAtOffset(1L).iterator().next().getId(), 0));
    insert(segment1);
    //
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(5)));

    Collection<Segment> result = fabricator.getPreviousSegmentsWithSameMainProgram();

    assertEquals(1, result.size());
  }

  @Test
  public void getMaxAvailableSequenceBindingOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Program program25 = newProgram(25, 3, 2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, now());
    Sequence sequence25a = program25.add(newSequence(0, "Chunk", 0.4, "G minor", 115.0));
    program25.add(newSequenceBinding(sequence25a, 0));
    UUID sequenceBindingId = program25.add(newSequenceBinding(sequence25a, 1)).getId();
    program25.add(newSequenceBinding(sequence25a, 2));
    insert(program25);
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertEquals(Long.valueOf(2L), fabricator.getMaxAvailableSequenceBindingOffset(new Choice()
      .setProgramId(BigInteger.valueOf(25))
      .setSegmentId(BigInteger.valueOf(4))
      .setSequenceBindingId(sequenceBindingId)));
  }

  @Test
  public void getNextSequenceBindingOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Program program25 = newProgram(25, 3, 2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, now());
    Sequence sequence25a = program25.add(newSequence(0, "Chunk", 0.4, "G minor", 115.0));
    UUID sequenceBindingId = program25.add(newSequenceBinding(sequence25a, 0)).getId();
    program25.add(newSequenceBinding(sequence25a, 1));
    program25.add(newSequenceBinding(sequence25a, 2));
    insert(program25);
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertEquals(Long.valueOf(1L), fabricator.getNextSequenceBindingOffset(new Choice()
      .setProgramId(BigInteger.valueOf(25))
      .setSegmentId(BigInteger.valueOf(4))
      .setSequenceBindingId(sequenceBindingId)));
  }

  @Test
  public void getNextSequenceBindingOffset_endLoopsBackToZero() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Program program25 = newProgram(25, 3, 2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, now());
    Sequence sequence25a = program25.add(newSequence(0, "Chunk", 0.4, "G minor", 115.0));
    program25.add(newSequenceBinding(sequence25a, 0));
    program25.add(newSequenceBinding(sequence25a, 1));
    UUID sequenceBindingId = program25.add(newSequenceBinding(sequence25a, 2)).getId();
    insert(program25);
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertEquals(Long.valueOf(0L), fabricator.getNextSequenceBindingOffset(new Choice()
      .setProgramId(BigInteger.valueOf(25))
      .setSegmentId(BigInteger.valueOf(4))
      .setSequenceBindingId(sequenceBindingId)));
  }

  @Test
  public void hasOneMoreSequenceBindingOffset_true() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Program program25 = newProgram(25, 3, 2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, now());
    Sequence sequence25a = program25.add(newSequence(0, "Chunk", 0.4, "G minor", 115.0));
    program25.add(newSequenceBinding(sequence25a, 0));
    UUID sequenceBindingId = program25.add(newSequenceBinding(sequence25a, 1)).getId();
    program25.add(newSequenceBinding(sequence25a, 2));
    insert(program25);
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertTrue(fabricator.hasOneMoreSequenceBindingOffset(new Choice()
      .setProgramId(BigInteger.valueOf(25))
      .setSegmentId(BigInteger.valueOf(4))
      .setSequenceBindingId(sequenceBindingId)));
  }

  @Test
  public void hasOneMoreSequenceBindingOffset_false() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Program program25 = newProgram(25, 3, 2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, now());
    Sequence sequence25a = program25.add(newSequence(0, "Chunk", 0.4, "G minor", 115.0));
    program25.add(newSequenceBinding(sequence25a, 0));
    UUID sequenceBindingId = program25.add(newSequenceBinding(sequence25a, 1)).getId();
    insert(program25);
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertFalse(fabricator.hasOneMoreSequenceBindingOffset(new Choice()
      .setProgramId(BigInteger.valueOf(25))
      .setSegmentId(BigInteger.valueOf(4))
      .setSequenceBindingId(sequenceBindingId)));
  }

  @Test
  public void hasTwoMoreSequenceBindingOffsets_true() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Program program25 = newProgram(25, 3, 2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, now());
    Sequence sequence25a = program25.add(newSequence(0, "Chunk", 0.4, "G minor", 115.0));
    program25.add(newSequenceBinding(sequence25a, 0));
    UUID sequenceBindingId = program25.add(newSequenceBinding(sequence25a, 1)).getId();
    program25.add(newSequenceBinding(sequence25a, 2));
    program25.add(newSequenceBinding(sequence25a, 3));
    insert(program25);
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertTrue(fabricator.hasTwoMoreSequenceBindingOffsets(new Choice()
      .setProgramId(BigInteger.valueOf(25))
      .setSegmentId(BigInteger.valueOf(4))
      .setSequenceBindingId(sequenceBindingId)));
  }


  /**
   Same conditions as that return true for hasOneMore offset, can be false for hasTwoMore
   */
  @Test
  public void hasTwoMoreSequenceBindingOffsets_false() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Program program25 = newProgram(25, 3, 2, ProgramType.Macro, ProgramState.Published, "Chunky Peanut Butter", "G minor", 120.0, now());
    Sequence sequence25a = program25.add(newSequence(0, "Chunk", 0.4, "G minor", 115.0));
    program25.add(newSequenceBinding(sequence25a, 0));
    UUID sequenceBindingId = program25.add(newSequenceBinding(sequence25a, 1)).getId();
    program25.add(newSequenceBinding(sequence25a, 2));
    insert(program25);
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertFalse(fabricator.hasTwoMoreSequenceBindingOffsets(new Choice()
      .setProgramId(BigInteger.valueOf(25))
      .setSegmentId(BigInteger.valueOf(4))
      .setSequenceBindingId(sequenceBindingId)));
  }

}
