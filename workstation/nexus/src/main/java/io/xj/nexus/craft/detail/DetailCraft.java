// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.detail;

import io.xj.nexus.FabricationException;

/**
 Structure craft for the current segment includes all kinds of Detail
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
@FunctionalInterface
public interface DetailCraft {

  /**
   Perform Detail craft for the current segment
   <p>
   Workstation fabrication Layering/Intensity
   https://github.com/xjmusic/workstation/issues/196
   */
  void doWork() throws FabricationException;

}
