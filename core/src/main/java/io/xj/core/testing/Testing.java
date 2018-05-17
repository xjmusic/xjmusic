// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.testing;

import java.util.Objects;

import static org.junit.Assert.fail;

/**
 Unit testing helpers
 */
public class Testing {

  /**
   Assert that the array of strings contains the result

   @param matches to match against
   @param result  to match
   */
  public static void assertIn(String[] matches, String result) {
    for (String match : matches) {
      if (Objects.equals(match, result))
        return;
    }
    fail("'" + result + "' is an invalid result");
  }

}
