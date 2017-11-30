// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.link_message;

import io.xj.core.exception.BusinessException;
import io.xj.core.model.message.MessageType;
import io.xj.core.tables.records.LinkMessageRecord;

import org.jooq.Field;
import org.jooq.types.ULong;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Map;

import static io.xj.core.Tables.LINK_MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LinkMessageTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new LinkMessage()
      .setLinkId(BigInteger.valueOf(2))
      .setType(MessageType.Warning)
      .setBody("This is a warning")
      .validate();
  }

  @Test
  public void validate_failsWithoutLinkID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Link ID is required");

    new LinkMessage()
      .setType(MessageType.Warning)
      .setBody("This is a warning")
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Type is required");

    new LinkMessage()
      .setLinkId(BigInteger.valueOf(2))
      .setBody("This is a warning")
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'sneeze' is not a valid type");

    new LinkMessage()
      .setType("sneeze")
      .setLinkId(BigInteger.valueOf(2))
      .setBody("This is a warning")
      .validate();
  }

  @Test
  public void validate_failsWithoutBody() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Body is required");

    new LinkMessage()
      .setLinkId(BigInteger.valueOf(2))
      .setType(MessageType.Warning)
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    LinkMessageRecord record = new LinkMessageRecord();
    record.setId(ULong.valueOf(12));
    record.setBody("This is a warning");
    record.setLinkId(ULong.valueOf(1235));
    record.setType(MessageType.Warning.toString());
    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));

    LinkMessage result = new LinkMessage()
      .setFromRecord(record);

    assertNotNull(result);
    assertEquals("This is a warning", result.getBody());
    assertEquals(ULong.valueOf(1235), result.getLinkId());
    assertEquals(MessageType.Warning, result.getType());
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new LinkMessage().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new LinkMessage()
      .setLinkId(BigInteger.valueOf(2))
      .setType(MessageType.Warning)
      .setBody("This is a warning")
      .updatableFieldValueMap();

    assertEquals("This is a warning", result.get(LINK_MESSAGE.BODY));
    assertEquals(ULong.valueOf(2), result.get(LINK_MESSAGE.LINK_ID));
    assertEquals(MessageType.Warning.toString(), result.get(LINK_MESSAGE.TYPE));
  }

}
