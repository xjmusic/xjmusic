//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.payload;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainConfig;
import io.xj.core.model.ChainConfigType;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentState;
import io.xj.core.model.InstrumentType;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.model.InstrumentAudioChord;
import io.xj.core.model.InstrumentAudioEvent;
import io.xj.core.model.InstrumentMeme;
import io.xj.core.model.Library;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.ProgramMeme;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramSequenceBinding;
import io.xj.core.model.ProgramSequenceBindingMeme;
import io.xj.core.model.ProgramSequenceChord;
import io.xj.core.model.User;
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
    assertFalse(subject.setDataEntity(Account.create("Test")).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetDataEntities() {
    assertFalse(subject.setDataEntities(ImmutableList.of(Account.create("Test"))).isEmpty());
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
    assertEquals(PayloadDataType.HasOne, subject.setDataEntity(Account.create("Test")).getDataType());
  }

  @Test
  public void type_hasMany_afterSetDataEntities() {
    assertEquals(PayloadDataType.HasMany, subject.setDataEntities(ImmutableList.of(Account.create("Test"))).getDataType());
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

  /**
   Serialize a payload comprising a Program
   <p>
   [#166690830] Program model handles all of its own entities

   @throws CoreException on failure
   */
  @Test
  public void setDataOne_program() throws CoreException {
    Program program = Program.create(User.create(), Library.create(), ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4, 0.6);
    subject.setDataEntity(program);
    subject.addIncluded(ProgramMeme.create(program, "Ants").toPayloadObject());
    ProgramSequence sequence902 = ProgramSequence.create(program, 16, "decay", 0.25, "F#", 110.3);
    subject.addIncluded(sequence902.toPayloadObject());
    subject.addIncluded(ProgramSequenceChord.create(sequence902, 0.0, "G minor").toPayloadObject());
    subject.addIncluded(ProgramSequenceChord.create(sequence902, 4.0, "C major").toPayloadObject());
    subject.addIncluded(ProgramSequenceChord.create(sequence902, 8.0, "F7").toPayloadObject());
    subject.addIncluded(ProgramSequenceChord.create(sequence902, 12.0, "G7").toPayloadObject());
    subject.addIncluded(ProgramSequenceChord.create(sequence902, 16.0, "F minor").toPayloadObject());
    subject.addIncluded(ProgramSequenceChord.create(sequence902, 20.0, "Bb major").toPayloadObject());
    ProgramSequenceBinding binding902_0 = ProgramSequenceBinding.create(sequence902, 0);
    subject.addIncluded(binding902_0.toPayloadObject());
    ProgramSequenceBinding binding902_1 = ProgramSequenceBinding.create(sequence902, 1);
    subject.addIncluded(binding902_1.toPayloadObject());
    ProgramSequenceBinding binding902_2 = ProgramSequenceBinding.create(sequence902, 2);
    subject.addIncluded(binding902_2.toPayloadObject());
    ProgramSequenceBinding binding902_3 = ProgramSequenceBinding.create(sequence902, 3);
    subject.addIncluded(binding902_3.toPayloadObject());
    ProgramSequenceBinding binding902_4 = ProgramSequenceBinding.create(sequence902, 4);
    subject.addIncluded(binding902_4.toPayloadObject());
    subject.addIncluded(ProgramSequenceBinding.create(sequence902, 5).toPayloadObject());
    subject.addIncluded(ProgramSequenceBindingMeme.create(binding902_0, "Gravel").toPayloadObject());
    subject.addIncluded(ProgramSequenceBindingMeme.create(binding902_1, "Gravel").toPayloadObject());
    subject.addIncluded(ProgramSequenceBindingMeme.create(binding902_2, "Gravel").toPayloadObject());
    subject.addIncluded(ProgramSequenceBindingMeme.create(binding902_3, "Rocks").toPayloadObject());
    subject.addIncluded(ProgramSequenceBindingMeme.create(binding902_1, "Fuzz").toPayloadObject());
    subject.addIncluded(ProgramSequenceBindingMeme.create(binding902_2, "Fuzz").toPayloadObject());
    subject.addIncluded(ProgramSequenceBindingMeme.create(binding902_3, "Fuzz").toPayloadObject());
    subject.addIncluded(ProgramSequenceBindingMeme.create(binding902_4, "Noise").toPayloadObject());

    assertTrue(subject.getLinks().isEmpty());
    assertTrue(subject.getDataOne().isPresent());
    assertEquals("programs", subject.getDataOne().get().getType());
    assertEquals(22, subject.getIncluded().size());
  }

  /**
   Serialize a payload comprising a Instrument
   <p>
   [#166708597] Instrument model handles all of its own entities

   @throws CoreException on failure
   */
  @Test
  public void setDataOne_instrument() throws CoreException {
    Instrument instrument = Instrument.create(User.create(), Library.create(), InstrumentType.Percussive, InstrumentState.Published, "808 Drums");
    subject.setDataEntity(instrument);
    subject.addIncluded(InstrumentMeme.create(instrument, "Ants").toPayloadObject());
    subject.addIncluded(InstrumentMeme.create(instrument, "Mold").toPayloadObject());
    InstrumentAudio audio402 = InstrumentAudio.create(instrument, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01, 2.123, 120.0, 440.0, 0.62);
    subject.addIncluded(audio402.toPayloadObject());
    subject.addIncluded(InstrumentAudioChord.create(audio402, 0.0, "E minor").toPayloadObject());
    subject.addIncluded(InstrumentAudioChord.create(audio402, 4.0, "A major").toPayloadObject());
    subject.addIncluded(InstrumentAudioChord.create(audio402, 8.0, "B minor").toPayloadObject());
    subject.addIncluded(InstrumentAudioChord.create(audio402, 12.0, "F# major").toPayloadObject());
    subject.addIncluded(InstrumentAudioChord.create(audio402, 16.0, "Ab7").toPayloadObject());
    subject.addIncluded(InstrumentAudioChord.create(audio402, 20.0, "Bb7").toPayloadObject());
    InstrumentAudio audio401 = InstrumentAudio.create(instrument, "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440.0, 0.62);
    subject.addIncluded(audio401.toPayloadObject());
    subject.addIncluded(InstrumentAudioEvent.create(audio401, 0.0, 1.0, "KICK", "Eb", 1.0).toPayloadObject());
    subject.addIncluded(InstrumentAudioEvent.create(audio401, 1.0, 1.0, "SNARE", "Ab", 0.8).toPayloadObject());
    subject.addIncluded(InstrumentAudioEvent.create(audio401, 2.5, 1.0, "KICK", "C", 1.0).toPayloadObject());
    subject.addIncluded(InstrumentAudioEvent.create(audio401, 3.0, 1.0, "SNARE", "B", 0.8).toPayloadObject());

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
    Chain chain = Chain.create(Account.create(), "Test Print #1", ChainType.Production, ChainState.Erase, Instant.parse("2014-08-12T12:17:02.527142Z"), Instant.parse("2014-08-12T12:19:02.527142Z"), null);
    subject.setDataEntity(chain);
    subject.addIncluded(ChainConfig.create(Chain.create(), ChainConfigType.OutputContainer, "aac").toPayloadObject());
    subject.addIncluded(ChainConfig.create(Chain.create(), ChainConfigType.OutputChannels, "4").toPayloadObject());
    subject.addIncluded(ChainBinding.create(chain, Library.create()).toPayloadObject());


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
    Chain chain = Chain.create(Account.create(), "Test Print #1", ChainType.Production, ChainState.Erase, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);
    subject.setDataEntity(chain);
    subject.addIncluded(ChainConfig.create(Chain.create(), ChainConfigType.OutputContainer, "aac").toPayloadObject());
    subject.addIncluded(ChainConfig.create(Chain.create(), ChainConfigType.OutputChannels, "4").toPayloadObject());
    subject.addIncluded(ChainBinding.create(chain, Library.create()).toPayloadObject());

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
    subject.setDataEntities(ImmutableList.of());

    assertEquals(PayloadDataType.HasMany, subject.getDataType());
  }


}
