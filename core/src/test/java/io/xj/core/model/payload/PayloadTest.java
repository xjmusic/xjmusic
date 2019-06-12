//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.payload;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainConfigType;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.instrument.sub.Audio;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PayloadTest extends CoreTest {
  private Payload subject;

  @Before
  public void setUp() throws CoreException {
    subject = new Payload();
  }

  @Test
  public void isEmpty() {
    assertTrue(subject.isEmpty());
    assertEquals(PayloadDataType.Ambiguous, subject.getDataType());
    assertFalse(subject.getDataOne().isPresent());
    assertTrue(subject.getDataMany().isEmpty());
    assertTrue(subject.getLinks().isEmpty());
    assertTrue(subject.getIncluded().isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetDataEntity() {
    assertFalse(subject.setDataEntity(newAccount(5, "Test")).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetDataEntities() {
    assertFalse(subject.setDataEntities(ImmutableList.of(newAccount(5, "Test")), false).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetData_one() {
    assertFalse(subject.setDataOne(new PayloadObject()).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetData_many() {
    assertFalse(subject.setDataMany(ImmutableList.of(new PayloadObject())).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterAddData() {
    assertFalse(subject.addData(new PayloadObject()).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetLinks() {
    assertFalse(subject.setLinks(ImmutableMap.of("One", "1", "Two", "2")).isEmpty());
  }

  @Test
  public void type_hasOne_afterSetDataEntity() {
    assertEquals(PayloadDataType.HasOne, subject.setDataEntity(newAccount(5, "Test")).getDataType());
  }

  @Test
  public void type_hasMany_afterSetDataEntities() {
    assertEquals(PayloadDataType.HasMany, subject.setDataEntities(ImmutableList.of(newAccount(5, "Test")), false).getDataType());
  }

  @Test
  public void type_hasOne_afterSetData_one() {
    assertEquals(PayloadDataType.HasOne, subject.setDataOne(new PayloadObject()).getDataType());
  }

  @Test
  public void type_hasMany_afterSetData_many() {
    assertEquals(PayloadDataType.HasMany, subject.setDataMany(ImmutableList.of(new PayloadObject())).getDataType());
  }

  @Test
  public void type_hasMany_afterAddData() {
    assertEquals(PayloadDataType.HasMany, subject.addData(new PayloadObject()).getDataType());
  }

  @Test
  public void setType() {
    assertEquals(PayloadDataType.HasOne, subject.setDataType(PayloadDataType.HasOne).getDataType());
  }

  @Test
  public void addIncluded() {
    assertEquals(1, subject.addIncluded(new PayloadObject()).getIncluded().size());
  }

  @Test
  public void setIncluded() {
    assertEquals(1, subject.setIncluded(ImmutableList.of(new PayloadObject())).getIncluded().size());
  }

  @Test
  public void includeSubEntitiesOf() throws CoreException {
    Chain chain = newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Erase, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-08-12T12:19:02.527142Z"), null, Instant.parse("2014-08-12T12:17:02.527142Z"));
    chain.add(newChainConfig(ChainConfigType.OutputContainer, "aac"));
    chain.add(newChainConfig(ChainConfigType.OutputChannels, "4"));
    chain.add(newChainBinding("Library", 27));

    assertEquals(3, subject.addIncluded(chain.getAllSubEntities()).getIncluded().size());
  }

  /**
   Serialize a payload comprising a Program
   <p>
   [#166690830] Program model handles all of its own entities

   @throws CoreException on failure
   */
  @Test
  public void setDataOne_program() throws CoreException {
    Program program = newProgram(701, 101, 10000001, ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4, Instant.parse("2014-08-12T12:17:02.527142Z"));
    program.add(newProgramMeme("Ants"));
    Sequence sequence902 = program.add(newSequence(16, "decay", 0.25, "F#", 110.3));
    program.add(newSequenceChord(sequence902, 0.0, "G minor"));
    program.add(newSequenceChord(sequence902, 4.0, "C major"));
    program.add(newSequenceChord(sequence902, 8.0, "F7"));
    program.add(newSequenceChord(sequence902, 12.0, "G7"));
    program.add(newSequenceChord(sequence902, 16.0, "F minor"));
    program.add(newSequenceChord(sequence902, 20.0, "Bb major"));
    SequenceBinding binding902_0 = program.add(newSequenceBinding(sequence902, 0));
    SequenceBinding binding902_1 = program.add(newSequenceBinding(sequence902, 1));
    SequenceBinding binding902_2 = program.add(newSequenceBinding(sequence902, 2));
    SequenceBinding binding902_3 = program.add(newSequenceBinding(sequence902, 3));
    SequenceBinding binding902_4 = program.add(newSequenceBinding(sequence902, 4));
    program.add(newSequenceBinding(sequence902, 5));
    program.add(newSequenceBindingMeme(binding902_0, "Gravel"));
    program.add(newSequenceBindingMeme(binding902_1, "Gravel"));
    program.add(newSequenceBindingMeme(binding902_2, "Gravel"));
    program.add(newSequenceBindingMeme(binding902_3, "Rocks"));
    program.add(newSequenceBindingMeme(binding902_1, "Fuzz"));
    program.add(newSequenceBindingMeme(binding902_2, "Fuzz"));
    program.add(newSequenceBindingMeme(binding902_3, "Fuzz"));
    program.add(newSequenceBindingMeme(binding902_4, "Noise"));
    subject.setDataEntity(program);

    assertTrue(subject.getLinks().isEmpty());
    assertTrue(subject.getDataOne().isPresent());
    assertEquals("programs", subject.getDataOne().get().getType());
    assertEquals(22, subject.getIncluded().size());
  }

  /**
   Serialize a payload comprising many Programs, but do NOT include sub-entities
   + for now this is key to NOT sending overwhelmingly large resource index payloads
   + FUTURE may include partial filter of sub-entities
   */
  @Test
  public void setDataMany_program_doesNotIncludeSubEntities() {
    Program program = newProgram(701, 101, 10000001, ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4, Instant.parse("2014-08-12T12:17:02.527142Z"));
    program.add(newProgramMeme("Ants"));
    Sequence sequence902 = program.add(newSequence(16, "decay", 0.25, "F#", 110.3));
    program.add(newSequenceChord(sequence902, 0.0, "G minor"));
    SequenceBinding binding902_0 = program.add(newSequenceBinding(sequence902, 0));
    program.add(newSequenceBinding(sequence902, 5));
    program.add(newSequenceBindingMeme(binding902_0, "Gravel"));
    subject.setDataEntities(ImmutableList.of(program), false);

    assertTrue(subject.getLinks().isEmpty());
    assertEquals(1, subject.getDataMany().size());
    assertEquals(0, subject.getIncluded().size());
  }

  /**
   Serialize a payload comprising a Instrument
   <p>
   [#166708597] Instrument model handles all of its own entities

   @throws CoreException on failure
   */
  @Test
  public void setDataOne_instrument() throws CoreException {
    Instrument instrument = newInstrument(201, 101, 10000001, InstrumentType.Percussive, InstrumentState.Published, "808 Drums", Instant.parse("2014-08-12T12:17:02.527142Z"));
    instrument.add(newInstrumentMeme("Ants"));
    instrument.add(newInstrumentMeme("Mold"));
    Audio audio402 = instrument.add(newAudio("Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01, 2.123, 120.0, 440.0, 0.62));
    instrument.add(newAudioChord(audio402, 0.0, "E minor"));
    instrument.add(newAudioChord(audio402, 4.0, "A major"));
    instrument.add(newAudioChord(audio402, 8.0, "B minor"));
    instrument.add(newAudioChord(audio402, 12.0, "F# major"));
    instrument.add(newAudioChord(audio402, 16.0, "Ab7"));
    instrument.add(newAudioChord(audio402, 20.0, "Bb7"));
    Audio audio401 = instrument.add(newAudio("Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0, 0.62));
    instrument.add(newAudioEvent(audio401, 0.0, 1.0, "KICK", "Eb", 1.0));
    instrument.add(newAudioEvent(audio401, 1.0, 1.0, "SNARE", "Ab", 0.8));
    instrument.add(newAudioEvent(audio401, 2.5, 1.0, "KICK", "C", 1.0));
    instrument.add(newAudioEvent(audio401, 3.0, 1.0, "SNARE", "B", 0.8));

    subject.setDataEntity(instrument);

    assertTrue(subject.getLinks().isEmpty());
    assertTrue(subject.getDataOne().isPresent());
    assertEquals("instruments", subject.getDataOne().get().getType());
    assertEquals(14, subject.getIncluded().size());
  }

  /**
   Serialize a payload comprising a Chain
   <p>
   [#166743281] Chain handles all of its own binding + config entities

   @throws CoreException on failure
   */
  @Test
  public void setDataOne_chain() throws CoreException {
    Chain chain = newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Erase, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-08-12T12:19:02.527142Z"), null, Instant.parse("2014-08-12T12:17:02.527142Z"));
    chain.add(newChainConfig(ChainConfigType.OutputContainer, "aac"));
    chain.add(newChainConfig(ChainConfigType.OutputChannels, "4"));
    chain.add(newChainBinding("Library", 27));

    subject.setDataEntity(chain);

    assertTrue(subject.getLinks().isEmpty());
    assertTrue(subject.getDataOne().isPresent());
    assertEquals("chains", subject.getDataOne().get().getType());
    assertEquals(3, subject.getIncluded().size());
  }

  /**
   Serialize a payload comprising a Chain having a null stop-at
   <p>
   [#166743281] Chain handles all of its own binding + config entities

   @throws CoreException on failure
   */
  @Test
  public void chain_havingNoStopAtTime() throws CoreException {
    Chain chain = newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Erase, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, Instant.parse("2014-08-12T12:17:02.527142Z"));
    chain.add(newChainConfig(ChainConfigType.OutputContainer, "aac"));
    chain.add(newChainConfig(ChainConfigType.OutputChannels, "4"));
    chain.add(newChainBinding("Library", 27));
    subject.setDataEntity(chain);

    assertTrue(subject.getLinks().isEmpty());
    assertTrue(subject.getDataOne().isPresent());
    assertEquals("chains", subject.getDataOne().get().getType());
    assertEquals(3, subject.getIncluded().size());
  }


  @Test
  public void setSelfURI() {
    assertEquals("https://hub.xj.io/api/1/things", subject.setSelfURI(URI.create("https://hub.xj.io/api/1/things")).getLinks().get("self"));
  }

  @Test
  public void getSelfURI() {
    subject.getLinks().put("self", "https://hub.xj.io/api/1/things");

    assertEquals(URI.create("https://hub.xj.io/api/1/things"), subject.getSelfURI());
  }

  @Test
  public void setDataEntities_empty_setsDataTypeToHasMany() {
    subject.setDataEntities(ImmutableList.of(), false);

    assertEquals(PayloadDataType.HasMany, subject.getDataType());
  }

  @Test
  public void setErrorsOf_SuperEntity() {
    Chain chain = newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Erase, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, Instant.parse("2014-08-12T12:17:02.527142Z"));
    chain.add(newChainConfig(ChainConfigType.OutputChannels, "Not a (required) numeric value"));

    assertEquals(1, subject.addErrorsOf(chain).getErrors().size());
  }
}
