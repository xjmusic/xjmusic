// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.entity;// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import com.google.common.collect.ImmutableMap;
import io.xj.core.CoreTest;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.user.User;
import io.xj.core.util.Text;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EntityTest extends CoreTest {

  @Test
  public void keyValueString() {
    assertEquals("Test{one=1,two=2,three=3}",
      Entity.keyValueString("Test", ImmutableMap.of("one", "1", "two", "2", "three", "3")));
  }

}
