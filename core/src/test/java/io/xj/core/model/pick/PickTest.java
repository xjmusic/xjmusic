// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pick;

import io.xj.core.exception.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class PickTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_failsWithoutArrangementID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Arrangement ID is required");

    new Pick()
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_failsWithoutPatternEventID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Pattern Event ID is required");

    new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_failsWithoutAudioID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Audio ID is required");

    new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Voice ID is required");

    new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_failsWithoutStart() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Start is required");

    new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_failsWithoutLength() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Length is required");

    new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setAmplitude(0.84)
      .setPitch(42.9)
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_failsWithoutAmplitude() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Amplitude is required");

    new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(2.7)
      .setPitch(42.9)
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_failsWithoutPitch() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Pitch is required");

    new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_lengthMinimumIfZero() throws Exception {
    Pick pick = new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(0.0)
      .setAmplitude(0.84)
      .setPitch(42.9);
    pick.setSegmentId(BigInteger.valueOf(25));
    pick.validate();

    assertEquals(Double.valueOf(0.01), pick.getLength());
  }

  @Test
  public void validate_amplitudeMinimumIfZero() throws Exception {
    Pick pick = new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.0)
      .setPitch(42.9);
    pick.setSegmentId(BigInteger.valueOf(25));
    pick.validate();

    assertEquals(Double.valueOf(0.01), pick.getAmplitude());
  }

  @Test
  public void validate_pitchMinimumIfZero() throws Exception {
    Pick pick = new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(0.0);
    pick.setSegmentId(BigInteger.valueOf(25));
    pick.validate();

    assertEquals(Double.valueOf(1.0), pick.getPitch());
  }

  @Test
  public void validate_lengthMinimumIfBelowZero() throws Exception {
    Pick pick = new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(-10.0)
      .setAmplitude(0.84)
      .setPitch(42.9);
    pick.setSegmentId(BigInteger.valueOf(25));
    pick.validate();

    assertEquals(Double.valueOf(0.01), pick.getLength());
  }

  @Test
  public void validate_amplitudeMinimumIfBelowZero() throws Exception {
    Pick pick = new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(-10.0)
      .setPitch(42.9);
    pick.setSegmentId(BigInteger.valueOf(25));
    pick.validate();

    assertEquals(Double.valueOf(0.01), pick.getAmplitude());
  }

  @Test
  public void validate_pitchMinimumIfBelowZero() throws Exception {
    Pick pick = new Pick()
      .setArrangementUuid(UUID.randomUUID())
      .setPatternEventId(BigInteger.valueOf(723L))
      .setAudioId(BigInteger.valueOf(6329L))
      .setVoiceId(BigInteger.valueOf(782L))
      .setInflection("TANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(-10.0);
    pick.setSegmentId(BigInteger.valueOf(25));
    pick.validate();

    assertEquals(Double.valueOf(1.0), pick.getPitch());
  }

  // TODO implement test for aggregate()

}
