// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.phase_chord;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.tables.records.PhaseChordRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.Tables.PHASE_CHORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PhaseChordTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new PhaseChord()
      .setName("C# minor")
      .setPhaseId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new PhaseChord()
      .setPhaseId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .validate();
  }

  @Test
  public void validate_failsWithoutPhaseID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Phase ID is required");

    new PhaseChord()
      .setName("C# minor")
      .setPosition(0.75)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new PhaseChord()
      .setName("C# minor")
      .setPhaseId(BigInteger.valueOf(1235))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    PhaseChordRecord record = new PhaseChordRecord();
    record.setId(ULong.valueOf(12));
    record.setName("C# minor");
    record.setPhaseId(ULong.valueOf(1235));
    record.setPosition(0.75);
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    PhaseChord result = new PhaseChord()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals("C# minor", result.getName());
    assertEquals(ULong.valueOf(1235), result.getPhaseId());
    assertEquals(Double.valueOf(0.75), result.getPosition());
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new PhaseChord().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new PhaseChord()
      .setName("C# minor")
      .setPhaseId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .updatableFieldValueMap();

    assertEquals("C# minor", result.get(PHASE_CHORD.NAME));
    assertEquals(ULong.valueOf(1235), result.get(PHASE_CHORD.PHASE_ID));
    assertEquals(0.75, result.get(PHASE_CHORD.POSITION));
  }

}
