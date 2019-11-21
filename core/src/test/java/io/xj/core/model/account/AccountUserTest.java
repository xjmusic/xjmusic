//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.account;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.AccountUser;
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

public class AccountUserTest extends CoreTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AccountUser subject;

  @Before
  public void setUp() {
    subject = new AccountUser();
  }

  @Test
  public void validate() throws Exception {
    subject
      .setUserId(UUID.randomUUID())
      .setAccountId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutAccountId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    subject
      .setUserId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutUserId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("User ID is required");

    subject
      .setAccountId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of(), subject.getResourceAttributeNames());
  }

  @Test
  public void setAllFrom() throws CoreException {
    PayloadObject payloadObject = new PayloadObject();
    UUID id = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();
    payloadObject
      .setId(id.toString())
      .setType("account-users")
      .add("account", Payload.referenceTo("accounts", accountId.toString()))
      .add("user", Payload.referenceTo("users", userId.toString()));

    subject.consume(payloadObject);

    assertEquals(id, subject.getId());
    assertEquals(accountId, subject.getAccountId());
    assertEquals(userId, subject.getUserId());
  }

  @Test
  public void toPayloadObject() {
    UUID userId = UUID.randomUUID();
    UUID accountId = UUID.randomUUID();
    UUID id = UUID.randomUUID();
    subject
      .setUserId(userId)
      .setAccountId(accountId)
      .setId(id);

    PayloadObject result = subject.toPayloadObject();

    assertEquals(id.toString(), result.getId());
    assertEquals("account-users", result.getType());
    assertTrue(result.getRelationships().get("account").getDataOne().isPresent());
    assertEquals(accountId.toString(), result.getRelationships().get("account").getDataOne().get().getId());
    assertTrue(result.getRelationships().get("user").getDataOne().isPresent());
    assertEquals(userId.toString(), result.getRelationships().get("user").getDataOne().get().getId());
  }
}
