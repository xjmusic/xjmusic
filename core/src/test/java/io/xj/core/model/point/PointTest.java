// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.point;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.Assert.assertNull;

public class PointTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Point()
      .setMorphId(BigInteger.valueOf(457832))
      .setVoiceEventId(BigInteger.valueOf(76943))
      .setPosition(1.25)
      .setNote("F")
      .setDuration(0.74)
      .validate();
  }

  @Test
  public void validate_failsWithoutMorphID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Morph ID is required");

    new Point()
      .setVoiceEventId(BigInteger.valueOf(76943))
      .setPosition(1.25)
      .setNote("F")
      .setDuration(0.74)
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceEventID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("VoiceEvent ID is required");

    new Point()
      .setMorphId(BigInteger.valueOf(457832))
      .setPosition(1.25)
      .setNote("F")
      .setDuration(0.74)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new Point()
      .setMorphId(BigInteger.valueOf(457832))
      .setVoiceEventId(BigInteger.valueOf(76943))
      .setNote("F")
      .setDuration(0.74)
      .validate();
  }

  @Test
  public void validate_failsWithoutDuration() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Duration is required");

    new Point()
      .setMorphId(BigInteger.valueOf(457832))
      .setVoiceEventId(BigInteger.valueOf(76943))
      .setPosition(1.25)
      .setNote("F")
      .validate();
  }

  @Test
  public void validate_failsWithoutNote() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Note is required");

    new Point()
      .setMorphId(BigInteger.valueOf(457832))
      .setVoiceEventId(BigInteger.valueOf(76943))
      .setPosition(1.25)
      .setDuration(0.74)
      .validate();
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Point().setFromRecord(null));
  }

}
