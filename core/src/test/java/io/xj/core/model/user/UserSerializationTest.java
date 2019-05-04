//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.user;

import com.google.gson.Gson;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.transport.GsonProvider;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

/**
 [#166132897] User model handles all of its own entities
 [#166273140] User Child Entities are identified and related by UUID (not id)
 */
public class UserSerializationTest {
  Gson gson = Guice.createInjector(new CoreModule()).getInstance(GsonProvider.class).gson();
  private User subject;
  private String subjectJson;

  private static void assertEquivalent(User u1, User u2) {
    assertEquals(u1.getId(), u2.getId());
    assertEquals(u1.getName(), u2.getName());
    assertEquals(u1.getAvatarUrl(), u2.getAvatarUrl());
    assertEquals(u1.getEmail(), u2.getEmail());
    assertEquals(u1.getRoles(), u2.getRoles());
  }

  @Before
  public void setUp() {
    subject = new User()
      .setId(BigInteger.valueOf(7))
      .setRoles("User,Admin")
      .setAvatarUrl("http://pictures.com/avatar.jpg")
      .setEmail("my@email.com")
      .setName("Joe");
    subject.setCreatedAt("2014-09-11 12:14:00.00");
    subject.setUpdatedAt("2014-09-11 12:15:00.00");
    subjectJson = "{\"id\":7,\"name\":\"Joe\",\"avatarUrl\":\"http://pictures.com/avatar.jpg\",\"email\":\"my@email.com\",\"roles\":\"User,Admin\"}";
  }

  @Test
  public void serialize() {
    String result = gson.toJson(subject);

    assertEquals(subjectJson, result);
  }

  @Test
  public void deserialize() {
    User result = gson.fromJson(subjectJson, User.class);

    assertEquivalent(subject, result);
  }

}
