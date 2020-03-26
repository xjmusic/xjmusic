// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.ValueException;
import io.xj.service.hub.testing.InternalResources;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
    Library target = Library.create(Account.create(), "Bananas", InternalResources.now());

    ChainBinding subject = new ChainBinding().setTarget(target);

    assertEquals(ChainBindingType.Library, subject.getType());
    assertEquals(target.getId(), subject.getTargetId());
  }

  @Test
  public void fromEntity() throws ValueException {
    Library target = Library.create(Account.create(), "Bananas", InternalResources.now());

    ChainBinding subject = ChainBinding.create(Chain.create(), target);

    assertEquals(ChainBindingType.Library, subject.getType());
    assertEquals(target.getId(), subject.getTargetId());
  }
}
