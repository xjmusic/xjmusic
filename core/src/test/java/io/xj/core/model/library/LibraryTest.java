// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.library;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Library;
import io.xj.core.payload.Payload;
import io.xj.core.payload.PayloadObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LibraryTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Library subject;

  @Before
  public void setUp() {
    subject = new Library();
  }

  @Test
  public void validate() throws Exception {
    subject
      .setAccountId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutAccountID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    subject
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    subject
      .setAccountId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("name"), subject.getResourceAttributeNames());
  }

  @Test
  public void setAllFrom() throws CoreException {
    PayloadObject payloadObject = new PayloadObject();
    UUID libraryId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    payloadObject
      .setId(libraryId.toString())
      .setType("libraries")
      .add("account", Payload.referenceTo("accounts", accountId.toString()));

    subject.consume(payloadObject);

    assertEquals(libraryId, subject.getId());
    assertEquals(accountId, subject.getAccountId());
  }

  @Test
  public void setAllFrom_exceptionOnWrongPayloadType() throws CoreException {
    PayloadObject payloadObject = new PayloadObject();
    payloadObject
      .setId("72")
      .setType("account-user")
      .add("account", Payload.referenceTo("accounts", "43"))
      .add("user", Payload.referenceTo("account-users", "27"));

    failure.expect(CoreException.class);
    failure.expectMessage("Cannot set single libraries-type entity create account-user-type payload object!");

    subject.consume(payloadObject);
  }

  @Test
  public void toPayloadObject() {
    subject
      .setName("Test Library")
      .setAccountId(UUID.randomUUID())
      .setId(UUID.randomUUID());

    PayloadObject result = subject.toPayloadObject();

    assertEquals(subject.getId().toString(), result.getId());
    assertEquals("libraries", result.getType());
    assertEquals("Test Library", result.getAttributes().get("name"));
    assertTrue(result.getRelationships().get("account").getDataOne().isPresent());
    assertEquals(subject.getAccountId().toString(), result.getRelationships().get("account").getDataOne().get().getId());
  }

}
