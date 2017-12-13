// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.instrument;

import io.xj.core.exception.BusinessException;
import io.xj.core.tables.records.InstrumentRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.INSTRUMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class InstrumentTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Instrument()
      .setLibraryId(BigInteger.valueOf(907834))
      .setType("Percussive")
      .setDensity(0.8)
      .setDescription("TR-808")
      .setUserId(BigInteger.valueOf(1128743))
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new Instrument()
      .setLibraryId(BigInteger.valueOf(907834))
      .setDensity(0.8)
      .setDescription("TR-808")
      .setUserId(BigInteger.valueOf(1128743))
      .validate();
  }

  @Test
  public void validate_failsWithoutLibraryID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Library ID is required");

    new Instrument()
      .setType("Percussive")
      .setDensity(0.8)
      .setDescription("TR-808")
      .setUserId(BigInteger.valueOf(1128743))
      .validate();
  }

  @Test
  public void validate_failsWithoutUserID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("User ID is required");

    new Instrument()
      .setLibraryId(BigInteger.valueOf(907834))
      .setType("Percussive")
      .setDensity(0.8)
      .setDescription("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutDescription() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Description is required");

    new Instrument()
      .setLibraryId(BigInteger.valueOf(907834))
      .setType("Percussive")
      .setDensity(0.8)
      .setUserId(BigInteger.valueOf(1128743))
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'butt' is not a valid type");

    new Instrument()
      .setLibraryId(BigInteger.valueOf(907834))
      .setType("butt")
      .setDensity(0.8)
      .setDescription("TR-808")
      .setUserId(BigInteger.valueOf(1128743))
      .validate();
  }

  @Test
  public void validate_failsWithoutDensity() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Density is required");

    new Instrument()
      .setLibraryId(BigInteger.valueOf(907834))
      .setType("Percussive")
      .setDescription("TR-808")
      .setUserId(BigInteger.valueOf(1128743))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    InstrumentRecord record = new InstrumentRecord();
    record.setId(ULong.valueOf(12));
    record.setLibraryId(ULong.valueOf(907834));
    record.setType("Percussive");
    record.setDensity(0.8);
    record.setDescription("TR-808");
    record.setUserId(ULong.valueOf(1128743));
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    Instrument result = new Instrument()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(907834), result.getLibraryId());
    assertEquals(InstrumentType.Percussive, result.getType());
    assertEquals(Double.valueOf(0.8), result.getDensity());
    assertEquals("TR-808", result.getDescription());
    assertEquals(ULong.valueOf(1128743), result.getUserId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new Instrument().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Instrument instrument = new Instrument()
      .setLibraryId(BigInteger.valueOf(907834))
      .setType("Percussive")
      .setDensity(0.8)
      .setDescription("TR-808")
      .setUserId(BigInteger.valueOf(1128743));
    instrument.validate();
    Map<Field, Object> result = instrument.updatableFieldValueMap();

    assertEquals(ULong.valueOf(907834), result.get(INSTRUMENT.LIBRARY_ID));
    assertEquals(InstrumentType.Percussive, result.get(INSTRUMENT.TYPE));
    assertEquals(0.8, result.get(INSTRUMENT.DENSITY));
    assertEquals("TR-808", result.get(INSTRUMENT.DESCRIPTION));
    assertEquals(ULong.valueOf(1128743), result.get(INSTRUMENT.USER_ID));
  }

}
