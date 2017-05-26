// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.basis;

/**
 Type of Macro-Craft, depending on previous link existence and choices
 */
public enum BasisType {
  Initial, // the first macro and main ideas in the chain
  Continue, // Main-Idea, if has Phase remaining.
  NextMain, // and Continue Macro-Idea, if has 2 or more phases remaining.
  NextMacro // such that the first phase of the next Macro-Idea will overlap (replacing) the last phase of the current Macro-Idea.
}
