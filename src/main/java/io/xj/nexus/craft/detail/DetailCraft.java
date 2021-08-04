// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.detail;

import io.xj.nexus.NexusException;

/**
 Structure craft for the current segment includes all kinds of Detail
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
@FunctionalInterface
public interface DetailCraft {

  /**
   perform craft for the current segment
   */
  void doWork() throws NexusException;

}
