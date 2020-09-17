// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.entity;

import io.xj.lib.util.ValueException;
import io.xj.service.hub.entity.Account;
import io.xj.service.hub.entity.Library;
import io.xj.service.hub.entity.Program;
import io.xj.service.hub.entity.ProgramMeme;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ChainBindingTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ChainBinding()
      .setChainId(UUID.randomUUID())
      .setTypeEnum(ChainBindingType.Library)
      .setTargetId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void custom_toString() {
    assertEquals("Binding[Library=050d2312-9a23-4fb4-a8ef-cc94e17ebe32]", new ChainBinding()
      .setChainId(UUID.randomUUID())
      .setTypeEnum(ChainBindingType.Library)
      .setTargetId(UUID.fromString("050d2312-9a23-4fb4-a8ef-cc94e17ebe32"))
      .toString());
  }

  @Test
  public void validate_failsWithoutChainId() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Chain ID is required");

    new ChainBinding()
      .setTypeEnum(ChainBindingType.Library)
      .setTargetId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithInvalidTargetClass() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("not a valid entity");

    ProgramMeme target = ProgramMeme.create(Program.create(), "blue");

    ChainBinding.create(Chain.create(), target);
  }

  @Test
  public void validate_failsWithoutTargetClass() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Type is required");

    new ChainBinding()
      .setChainId(UUID.randomUUID())
      .setTargetId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutTargetId() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Chain-bound target ID is required");

    new ChainBinding()
      .setChainId(UUID.randomUUID())
      .setTypeEnum(ChainBindingType.Library)
      .validate();
  }

  @Test
  public void setTarget() throws ValueException {
    Library target = Library.create(Account.create(), "Bananas", Instant.now());

    ChainBinding subject = new ChainBinding().setTarget(target);

    assertEquals(ChainBindingType.Library, subject.getType());
    assertEquals(target.getId(), subject.getTargetId());
  }

  @Test
  public void fromEntity() throws ValueException {
    Library target = Library.create(Account.create(), "Bananas", Instant.now());

    ChainBinding subject = ChainBinding.create(Chain.create(), target);

    assertEquals(ChainBindingType.Library, subject.getType());
    assertEquals(target.getId(), subject.getTargetId());
  }
}
