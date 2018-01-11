// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.work.basis;

/**
 Type of Macro-Craft, depending on previous link existence and choices
 */
public enum BasisType {
  Initial, // the first macro and main patterns in the chain
  Continue, // Main-Pattern, if has Phase remaining.
  NextMain, // and Continue Macro-Pattern, if has 2 or more phases remaining.
  NextMacro // such that the first phase of the next Macro-Pattern will overlap (replacing) the last phase of the current Macro-Pattern.
}
