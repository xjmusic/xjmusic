// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.account;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.payload.PayloadObject;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class AccountTest extends CoreTest {

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
    assertSameItems(ImmutableList.of("createdAt", "updatedAt", "name"), subject.getResourceAttributeNames());
  }

  @Test
  public void setAllFrom() throws CoreException {
    PayloadObject obj = new PayloadObject()
      .setId("27")
      .setType("accounts")
      .setAttributes(ImmutableMap.of("name", "Test Account"));

    subject.consume(obj);

    assertEquals(BigInteger.valueOf(27), subject.getId());
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
      .setId(BigInteger.valueOf(72));

    PayloadObject result = account.toPayloadObject();

    assertEquals("72", result.getId());
    assertEquals("accounts", result.getType());
    assertEquals("Test Account", result.getAttributes().get("name"));
  }
}
