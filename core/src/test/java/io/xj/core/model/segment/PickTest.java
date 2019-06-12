//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.segment.sub.Pick;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

public class PickTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Pick()
      .setArrangementId(UUID.randomUUID())
      .setPatternEventId(UUID.randomUUID())
      .setAudioId(UUID.randomUUID())
      .setVoiceId(UUID.randomUUID())
      .setInflection("CLANG")
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
      .setPatternEventId(UUID.randomUUID())
      .setAudioId(UUID.randomUUID())
      .setVoiceId(UUID.randomUUID())
      .setInflection("CLANG")
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
      .setArrangementId(UUID.randomUUID())
      .setAudioId(UUID.randomUUID())
      .setVoiceId(UUID.randomUUID())
      .setInflection("CLANG")
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
      .setArrangementId(UUID.randomUUID())
      .setPatternEventId(UUID.randomUUID())
      .setVoiceId(UUID.randomUUID())
      .setInflection("CLANG")
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
      .setArrangementId(UUID.randomUUID())
      .setPatternEventId(UUID.randomUUID())
      .setAudioId(UUID.randomUUID())
      .setInflection("CLANG")
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
      .setArrangementId(UUID.randomUUID())
      .setPatternEventId(UUID.randomUUID())
      .setAudioId(UUID.randomUUID())
      .setVoiceId(UUID.randomUUID())
      .setInflection("CLANG")
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
      .setArrangementId(UUID.randomUUID())
      .setPatternEventId(UUID.randomUUID())
      .setAudioId(UUID.randomUUID())
      .setVoiceId(UUID.randomUUID())
      .setInflection("CLANG")
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
      .setArrangementId(UUID.randomUUID())
      .setPatternEventId(UUID.randomUUID())
      .setAudioId(UUID.randomUUID())
      .setVoiceId(UUID.randomUUID())
      .setInflection("CLANG")
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
      .setArrangementId(UUID.randomUUID())
      .setPatternEventId(UUID.randomUUID())
      .setAudioId(UUID.randomUUID())
      .setVoiceId(UUID.randomUUID())
      .setInflection("CLANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_exceptionOnLengthBelowMinimum() throws Exception {
    Pick pick = new Pick()
      .setArrangementId(UUID.randomUUID())
      .setPatternEventId(UUID.randomUUID())
      .setAudioId(UUID.randomUUID())
      .setVoiceId(UUID.randomUUID())
      .setInflection("CLANG")
      .setStart(0.92)
      .setLength(0.0)
      .setAmplitude(0.84)
      .setPitch(42.9);
    pick.setSegmentId(BigInteger.valueOf(25));

    failure.expect(CoreException.class);
    failure.expectMessage("must be at least 0.01");

    pick.validate();
  }

  @Test
  public void validate_exceptionOnAmplitudeBelowMinimum() throws Exception {
    Pick pick = new Pick()
      .setArrangementId(UUID.randomUUID())
      .setPatternEventId(UUID.randomUUID())
      .setAudioId(UUID.randomUUID())
      .setVoiceId(UUID.randomUUID())
      .setInflection("CLANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(-1.0)
      .setPitch(42.9);
    pick.setSegmentId(BigInteger.valueOf(25));

    failure.expect(CoreException.class);
    failure.expectMessage("must be at least 0.0");

    pick.validate();
  }

  @Test
  public void validate_exceptionOnPitchBelowMinimum() throws Exception {
    Pick pick = new Pick()
      .setArrangementId(UUID.randomUUID())
      .setPatternEventId(UUID.randomUUID())
      .setAudioId(UUID.randomUUID())
      .setVoiceId(UUID.randomUUID())
      .setInflection("CLANG")
      .setStart(0.92)
      .setLength(2.7)
      .setAmplitude(0.84)
      .setPitch(0.0);
    pick.setSegmentId(BigInteger.valueOf(25));

    failure.expect(CoreException.class);
    failure.expectMessage("must be at least 1.0");

    pick.validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("start", "length", "amplitude", "pitch", "inflection"), new Pick().getResourceAttributeNames());
  }


  // FUTURE implement test for minimum values for pitch, amplitude, length

}
