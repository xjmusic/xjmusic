// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.payload.PayloadObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.lib.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AccountTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Account subject;

  @Before
  public void setUp() {
    subject = new Account();
  }

  @Test
  public void validate() throws Exception {
    subject
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Account name is required");

    subject
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("name"), subject.getResourceAttributeNames());
  }

  @Test
  public void setAllFrom() throws CoreException {
    UUID id = UUID.randomUUID();
    PayloadObject obj = new PayloadObject()
      .setId(id.toString())
      .setType("accounts")
      .setAttributes(ImmutableMap.of("name", "Test Account"));

    subject.consume(obj);

    assertEquals(id, subject.getId());
    assertEquals("Test Account", subject.getName());
  }

  @Test
  public void setAllFrom_noId() throws CoreException {
    PayloadObject obj = new PayloadObject()
      .setType("accounts")
      .setAttributes(ImmutableMap.of("name", "Test Account"));

    subject.consume(obj);

    assertNull(subject.getId());
    assertEquals("Test Account", subject.getName());
  }

  @Test
  public void toPayloadObject() {
    Account account = subject;
    account
      .setName("Test Account")
      .setId(UUID.randomUUID());

    PayloadObject result = account.toPayloadObject();

    assertEquals(account.getId().toString(), result.getId());
    assertEquals("accounts", result.getType());
    assertEquals("Test Account", result.getAttributes().get("name"));
  }
}
