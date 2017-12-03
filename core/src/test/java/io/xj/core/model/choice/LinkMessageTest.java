// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.choice;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.tables.records.ChoiceRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.CHOICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LinkMessageTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setPatternId(BigInteger.valueOf(125))
      .setTranspose(5)
      .setType("Macro")
      .setPhaseOffset(BigInteger.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutLinkID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Link ID is required");

    new Choice()
      .setPatternId(BigInteger.valueOf(125))
      .setTranspose(5)
      .setType("Macro")
      .setPhaseOffset(BigInteger.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutPatternID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern ID is required");

    new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setTranspose(5)
      .setType("Macro")
      .setPhaseOffset(BigInteger.valueOf(4))
      .validate();
  }

  @Test
  public void validate_transposeZeroByDefault() throws Exception {
    Choice result = new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setPatternId(BigInteger.valueOf(125))
      .setType("Macro")
      .setPhaseOffset(BigInteger.valueOf(4));

    result.validate();

    assertEquals(Integer.valueOf(0), result.getTranspose());
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setPatternId(BigInteger.valueOf(125))
      .setTranspose(5)
      .setPhaseOffset(BigInteger.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'bung' is not a valid type");

    new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setPatternId(BigInteger.valueOf(125))
      .setTranspose(5)
      .setType("bung")
      .setPhaseOffset(BigInteger.valueOf(4))
      .validate();
  }

  @Test
  public void validate_failsWithoutPhaseOffset() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Phase Offset is required");

    new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setPatternId(BigInteger.valueOf(125))
      .setTranspose(5)
      .setType("Macro")
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    ChoiceRecord record = new ChoiceRecord();
    record.setId(ULong.valueOf(12));
    record.setLinkId(ULong.valueOf(352));
    record.setPatternId(ULong.valueOf(125));
    record.setTranspose(5);
    record.setType("Macro");
    record.setPhaseOffset(ULong.valueOf(4));
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Choice result = new Choice()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(352), result.getLinkId());
    assertEquals(ULong.valueOf(125), result.getPatternId());
    assertEquals(Integer.valueOf(5), result.getTranspose());
    assertEquals(PatternType.Macro, result.getType());
    assertEquals(ULong.valueOf(4), result.getPhaseOffset());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Choice().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Choice choice = new Choice()
      .setLinkId(BigInteger.valueOf(352))
      .setPatternId(BigInteger.valueOf(125))
      .setTranspose(5)
      .setType("Macro")
      .setPhaseOffset(BigInteger.valueOf(4));
    choice.validate();

    Map<Field, Object> result = choice.updatableFieldValueMap();

    assertEquals(ULong.valueOf(352), result.get(CHOICE.LINK_ID));
    assertEquals(ULong.valueOf(125), result.get(CHOICE.PATTERN_ID));
    assertEquals(5, result.get(CHOICE.TRANSPOSE));
    assertEquals(PatternType.Macro, result.get(CHOICE.TYPE));
    assertEquals(ULong.valueOf(4), result.get(CHOICE.PHASE_OFFSET));
  }

}
