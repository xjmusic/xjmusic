// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator;

/**
 Type of Macro-Craft, depending on previous segment existence and choices
 */
public enum FabricatorType {
  Initial, // Only the first Segment in a Chain: Choose first Macro and Main program.
  Continue, // Continue the Main Program (and implicitly, continue the Macro program too)
  NextMain, // Choose the next Main program (but continue the Macro program)
  NextMacro // Choose the next Macro program (and implicitly, the next Main program too)
}
