// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.entity;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class SegmentChoiceArrangementPickTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SegmentChoiceArrangementPick()
      .setSegmentChoiceArrangementId(UUID.randomUUID())
      .setProgramSequencePatternEventId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("CLANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutArrangementID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Arrangement ID is required");

    new SegmentChoiceArrangementPick()
      .setProgramSequencePatternEventId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("CLANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutPatternEventID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Pattern Event ID is required");

    new SegmentChoiceArrangementPick()
      .setSegmentChoiceArrangementId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("CLANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutAudioID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Audio ID is required");

    new SegmentChoiceArrangementPick()
      .setSegmentChoiceArrangementId(UUID.randomUUID())
      .setProgramSequencePatternEventId(UUID.randomUUID())
      .setName("CLANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutStart() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Start is required");

    new SegmentChoiceArrangementPick()
      .setSegmentChoiceArrangementId(UUID.randomUUID())
      .setProgramSequencePatternEventId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("CLANG")
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutLength() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Length is required");

    new SegmentChoiceArrangementPick()
      .setSegmentChoiceArrangementId(UUID.randomUUID())
      .setProgramSequencePatternEventId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("CLANG")
      .setStart(0.92)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutAmplitude() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Amplitude is required");

    new SegmentChoiceArrangementPick()
      .setSegmentChoiceArrangementId(UUID.randomUUID())
      .setProgramSequencePatternEventId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("CLANG")
      .setStart(0.92)
      .setLength(2.7)
      .setPitch(42.9)
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutPitch() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Pitch is required");

    new SegmentChoiceArrangementPick()
      .setSegmentChoiceArrangementId(UUID.randomUUID())
      .setProgramSequencePatternEventId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("CLANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setSegmentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_exceptionOnLengthBelowMinimum() throws Exception {
    SegmentChoiceArrangementPick pick = new SegmentChoiceArrangementPick()
      .setSegmentChoiceArrangementId(UUID.randomUUID())
      .setProgramSequencePatternEventId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("CLANG")
      .setStart(0.92)
      .setLength(0.0)
      .setAmplitude(0.84)
      .setPitch(42.9);
    pick.setSegmentId(UUID.randomUUID());

    failure.expect(ValueException.class);
    failure.expectMessage("must be at least 0.01");

    pick.validate();
  }

  @Test
  public void validate_exceptionOnAmplitudeBelowMinimum() throws Exception {
    SegmentChoiceArrangementPick pick = new SegmentChoiceArrangementPick()
      .setSegmentChoiceArrangementId(UUID.randomUUID())
      .setProgramSequencePatternEventId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("CLANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(-1.0)
      .setPitch(42.9);
    pick.setSegmentId(UUID.randomUUID());

    failure.expect(ValueException.class);
    failure.expectMessage("must be at least 0.0");

    pick.validate();
  }

  @Test
  public void validate_exceptionOnPitchBelowMinimum() throws Exception {
    SegmentChoiceArrangementPick pick = new SegmentChoiceArrangementPick()
      .setSegmentChoiceArrangementId(UUID.randomUUID())
      .setProgramSequencePatternEventId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("CLANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(0.0);
    pick.setSegmentId(UUID.randomUUID());

    failure.expect(ValueException.class);
    failure.expectMessage("must be at least 1.0");

    pick.validate();
  }

}
