//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.account;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.payload.PayloadObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

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
      .setUserId(BigInteger.valueOf(125434L))
      .setAccountId(BigInteger.valueOf(125434L))
      .validate();
  }

  @Test
  public void validate_failsWithoutAccountId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    subject
      .setUserId(BigInteger.valueOf(125434L))
      .validate();
  }

  @Test
  public void validate_failsWithoutUserId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("User ID is required");

    subject
      .setAccountId(BigInteger.valueOf(125434L))
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("updatedAt", "createdAt"), subject.getResourceAttributeNames());
  }

  @Test
  public void setAllFrom() throws CoreException {
    PayloadObject payloadObject = new PayloadObject();
    payloadObject
      .setId("72")
      .setType("account-users")
      .add("account", Payload.referenceTo("accounts", "43"))
      .add("user", Payload.referenceTo("users", "14"));

    subject.consume(payloadObject);

    assertEquals(BigInteger.valueOf(72), subject.getId());
    assertEquals(BigInteger.valueOf(43), subject.getAccountId());
    assertEquals(BigInteger.valueOf(14), subject.getUserId());
  }

  @Test
  public void toPayloadObject() {
    subject
      .setUserId(BigInteger.valueOf(14))
      .setAccountId(BigInteger.valueOf(43))
      .setId(BigInteger.valueOf(72));

    PayloadObject result = subject.toPayloadObject();

    assertEquals("72", result.getId());
    assertEquals("account-users", result.getType());
    assertTrue(result.getRelationships().get("account").getDataOne().isPresent());
    assertEquals("43", result.getRelationships().get("account").getDataOne().get().getId());
    assertTrue(result.getRelationships().get("user").getDataOne().isPresent());
    assertEquals("14", result.getRelationships().get("user").getDataOne().get().getId());
  }
}
