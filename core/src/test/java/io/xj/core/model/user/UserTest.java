// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.user;

import io.xj.core.app.exception.BusinessException;

import org.jooq.Field;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Map;

import static io.xj.core.Tables.USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class UserTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new User()
      .setRoles("user,artist")
      .setEmail("charneykaye@gmail.com")
      .setAvatarUrl("http://www.google.com/images/charneykaye.jpg")
      .setName("Charney Kaye")
      .validate();
  }

  @Test
  public void validate_withMinimalAttributes() throws Exception {
    new User()
      .setRoles("user,artist")
      .validate();
  }

  @Test
  public void validate_failsWithoutRoles() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("User roles required");

    new User()
      .validate();
  }

  @Test
  public void setFromRecord() throws Exception {
    // TODO: figure out how to add the custom "roles" column to the jOOQ record being passed in -- now failing with: java.lang.IllegalArgumentException: Field ("table"."column") is not contained in Row ("xj"."user"."id", "xj"."user"."name", "xj"."user"."email", "xj"."user"."avatar_url", "xj"."user"."created_at", "xj"."user"."updated_at")
//    UserRecord record = new UserRecord();
//    record.setId(ULong.valueOf(12));
//    record.setContent(User.FIELD_ROLES, "user,artist");
//    record.setEmail("charneykaye@gmail.com");
//    record.setAvatarUrl("http://www.google.com/images/charneykaye.jpg");
//    record.setName("Charney Kaye");
//
//    record.setCreatedAt(Timestamp.valueOf("2014-08-12 12:17:02.527142"));
//    record.setUpdatedAt(Timestamp.valueOf("2014-09-12 12:17:01.047563"));
//
//    User result = new User()
//      .of(record);
//
//    assertNotNull(result);
//    assertEquals(ULong.valueOf(12), result.getId());
//    assertEquals("user,artist", result.getRoles());
//    assertEquals("charneykaye@gmail.com", result.getEmail());
//    assertEquals("http://www.google.com/images/charneykaye.jpg", result.getAvatarUrl());
//    assertEquals("Charney Kaye", result.getName());
//    assertEquals(Timestamp.valueOf("2014-08-12 12:17:02.527142"), result.getCreatedAt());
//    assertEquals(Timestamp.valueOf("2014-09-12 12:17:01.047563"), result.getUpdatedAt());
  }

  @Test
  public void setFromRecord_nullPassesThrough() throws Exception {
    assertNull(new User().setFromRecord(null));
  }

  @Test
  public void intoFieldValueMap() throws Exception {
    Map<Field, Object> result = new User()
      .setEmail("charneykaye@gmail.com")
      .setAvatarUrl("http://www.google.com/images/charneykaye.jpg")
      .setName("Charney Kaye")
      .updatableFieldValueMap();

    assertEquals("charneykaye@gmail.com", result.get(USER.EMAIL));
    assertEquals("http://www.google.com/images/charneykaye.jpg", result.get(USER.AVATAR_URL));
    assertEquals("Charney Kaye", result.get(USER.NAME));
  }

}
