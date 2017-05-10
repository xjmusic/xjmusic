// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.craft.impl;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.function.Consumer;

/**
 Type of Macro-Craft, depending on previous link existence and choices
 */
public enum MacroCraftType {
  /*
    Initial("Initial"),
    Continue("Continue"),
    NextMain("NextMain"),
    NextMacro("NextMacro");
  */

  Initial, // the first macro and main ideas in the chain
  Continue, // Main-Idea, if has Phase remaining.
  NextMain, // and Continue Macro-Idea, if has 2 or more phases remaining.
  NextMacro; // such that the first phase of the next Macro-Idea will overlap (replacing) the last phase of the current Macro-Idea.

  private static final List<MacroCraftType> allMacroCraftTypes = ImmutableList.copyOf(MacroCraftType.values());

/*
  private final String value;
  MacroCraftType(String value) {
    this.value = value;
  }
*/

  /**
   Perform an action for all possible intervals

   @param action to perform
   */
  static public void forAll(Consumer<? super MacroCraftType> action) {
    allMacroCraftTypes.forEach(action);
  }

  /**
   Get interval from integer value

   @param value to get interval for
   @return interval
   */
  public static MacroCraftType valueOf(int value) {
    return MacroCraftType.values()[value - 1];
  }

/*
   public String getValue() {
    return value;
  }
*/
}
