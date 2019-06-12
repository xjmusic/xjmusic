//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.sub.ChainBinding;
import io.xj.core.model.library.Library;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

public class ChainBindingTest extends CoreTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ChainBinding()
      .setChainId(BigInteger.valueOf(125434L))
      .setTargetClass("Library")
      .setTargetId(BigInteger.valueOf(2))
      .validate();
  }

  @Test
  public void validate_failsWithoutChainId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Chain ID is required");

    new ChainBinding()
      .setTargetClass("Library")
      .setTargetId(BigInteger.valueOf(2))
      .validate();
  }

  @Test
  public void validate_failsWithoutTargetClass() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Chain-bound target class is required");

    new ChainBinding()
      .setChainId(BigInteger.valueOf(125434L))
      .setTargetId(BigInteger.valueOf(2))
      .validate();
  }

  @Test
  public void validate_failsWithoutTargetId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Chain-bound target ID is required");

    new ChainBinding()
      .setChainId(BigInteger.valueOf(125434L))
      .setTargetClass("Library")
      .validate();
  }

  @Test
  public void setTarget() throws CoreException {
    Library target = newLibrary(5, 12, "Bananas", now());

    ChainBinding subject = new ChainBinding().setTarget(target);

    assertEquals("Library", subject.getTargetClass());
    assertEquals(BigInteger.valueOf(5), subject.getTargetId());
  }

  @Test
  public void fromEntity() throws CoreException {
    Library target = newLibrary(5, 12, "Bananas", now());

    ChainBinding subject = ChainBinding.from(target);

    assertEquals("Library", subject.getTargetClass());
    assertEquals(BigInteger.valueOf(5), subject.getTargetId());
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("targetClass", "targetId"), new ChainBinding().getResourceAttributeNames());
  }

}
