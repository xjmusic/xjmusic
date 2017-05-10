// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.link_meme;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.tables.records.LinkMemeRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.outright.xj.core.Tables.LINK_MEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LinkMemeTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new LinkMeme()
      .setLinkId(BigInteger.valueOf(23678))
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutLinkID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Link ID is required");

    new LinkMeme()
      .setName("Miccheckonetwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Name is required");

    new LinkMeme()
      .setLinkId(BigInteger.valueOf(23678))
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    LinkMemeRecord record = new LinkMemeRecord();
    record.setId(ULong.valueOf(12));
    record.setLinkId(ULong.valueOf(23678));
    record.setName("Miccheckonetwo");
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    LinkMeme result = new LinkMeme()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(23678), result.getLinkId());
    assertEquals("Miccheckonetwo", result.getName());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new LinkMeme().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new LinkMeme()
      .setLinkId(BigInteger.valueOf(23678))
      .setName("Miccheckonetwo")
      .updatableFieldValueMap();

    assertEquals(ULong.valueOf(23678), result.get(LINK_MEME.LINK_ID));
    assertEquals("Miccheckonetwo", result.get(LINK_MEME.NAME));
  }

}
