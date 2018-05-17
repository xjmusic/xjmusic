// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.basis;

/**
 Type of Macro-Craft, depending on previous segment existence and choices
 */
public enum BasisType {
  Initial, // the first macro and main sequences in the chain
  Continue, // Main-Sequence, if has Pattern remaining.
  NextMain, // and Continue Macro-Sequence, if has 2 or more patterns remaining.
  NextMacro // such that the first pattern of the next Macro-Sequence will overlap (replacing) the last pattern of the current Macro-Sequence.
}
