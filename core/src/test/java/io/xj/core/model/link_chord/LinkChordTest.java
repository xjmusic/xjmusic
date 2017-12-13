// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.link_chord;

import io.xj.core.exception.BusinessException;
import io.xj.core.tables.records.LinkChordRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.LINK_CHORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LinkChordTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new LinkChord()
      .setName("C# minor")
      .setLinkId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new LinkChord()
      .setLinkId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .validate();
  }

  @Test
  public void validate_failsWithoutLinkID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Link ID is required");

    new LinkChord()
      .setName("C# minor")
      .setPosition(0.75)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Position is required");

    new LinkChord()
      .setName("C# minor")
      .setLinkId(BigInteger.valueOf(1235))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    LinkChordRecord record = new LinkChordRecord();
    record.setId(ULong.valueOf(12));
    record.setName("C# minor");
    record.setLinkId(ULong.valueOf(1235));
    record.setPosition(0.75);
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    LinkChord result = new LinkChord()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals("C# minor", result.getName());
    assertEquals(ULong.valueOf(1235), result.getLinkId());
    assertEquals(Double.valueOf(0.75), result.getPosition());
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new LinkChord().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new LinkChord()
      .setName("C# minor")
      .setLinkId(BigInteger.valueOf(1235))
      .setPosition(0.75)
      .updatableFieldValueMap();

    assertEquals("C# minor", result.get(LINK_CHORD.NAME));
    assertEquals(ULong.valueOf(1235), result.get(LINK_CHORD.LINK_ID));
    assertEquals(0.75, result.get(LINK_CHORD.POSITION));
  }

}
