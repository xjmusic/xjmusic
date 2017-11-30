// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.idea;

import io.xj.core.exception.BusinessException;
import io.xj.core.tables.records.IdeaRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.IDEA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class IdeaTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Idea()
      .setLibraryId(BigInteger.valueOf(23))
      .setType("Main")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987))
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new Idea()
      .setLibraryId(BigInteger.valueOf(23))
      .setType("Main")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987))
      .validate();
  }

  @Test
  public void validate_failsWithoutLibraryID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Library ID is required");

    new Idea()
      .setType("Main")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987))
      .validate();
  }

  @Test
  public void validate_failsWithoutUserID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("User ID is required");

    new Idea()
      .setLibraryId(BigInteger.valueOf(23))
      .setType("Main")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new Idea()
      .setLibraryId(BigInteger.valueOf(23))
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'funk' is not a valid type");

    new Idea()
      .setLibraryId(BigInteger.valueOf(23))
      .setType("funk")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987))
      .validate();
  }

  @Test
  public void validate_failsWithoutKey() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Key is required");

    new Idea()
      .setLibraryId(BigInteger.valueOf(23))
      .setType("Main")
      .setDensity(0.75)
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987))
      .validate();
  }

  @Test
  public void validate_failsWithoutDensity() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Density is required");

    new Idea()
      .setLibraryId(BigInteger.valueOf(23))
      .setType("Main")
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987))
      .validate();
  }

  @Test
  public void validate_failsWithoutTempo() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Tempo is required");

    new Idea()
      .setLibraryId(BigInteger.valueOf(23))
      .setType("Main")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setUserId(BigInteger.valueOf(987))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    IdeaRecord record = new IdeaRecord();
    record.setId(ULong.valueOf(12));
    record.setLibraryId(ULong.valueOf(23));
    record.setType("Main");
    record.setDensity(0.75);
    record.setKey("D# major 7");
    record.setName("Mic Check One Two");
    record.setTempo(120.0);
    record.setUserId(ULong.valueOf(987));
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Idea result = new Idea()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(23), result.getLibraryId());
    assertEquals(IdeaType.Main, result.getType());
    assertEquals(Double.valueOf(0.75), result.getDensity());
    assertEquals("D# major 7", result.getKey());
    assertEquals("Mic Check One Two", result.getName());
    assertEquals(Double.valueOf(120.0), result.getTempo());
    assertEquals(ULong.valueOf(987), result.getUserId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Idea().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Idea idea = new Idea()
      .setLibraryId(BigInteger.valueOf(23))
      .setType("Main")
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .setUserId(BigInteger.valueOf(987));
    idea.validate();
    Map<Field, Object> result = idea.updatableFieldValueMap();

    assertEquals(ULong.valueOf(23), result.get(IDEA.LIBRARY_ID));
    assertEquals(IdeaType.Main, result.get(IDEA.TYPE));
    assertEquals(0.75, result.get(IDEA.DENSITY));
    assertEquals("D# major 7", result.get(IDEA.KEY));
    assertEquals("Mic Check One Two", result.get(IDEA.NAME));
    assertEquals(120.0, result.get(IDEA.TEMPO));
    assertEquals(ULong.valueOf(987), result.get(IDEA.USER_ID));
  }

}
