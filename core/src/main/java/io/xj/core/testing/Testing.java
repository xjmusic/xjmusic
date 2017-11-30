// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.testing;

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
      if (match.equals(result))
        return;
    }
    fail("'" + result + "' is an invalid result");
  }

}
