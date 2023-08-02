// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.hook;

import io.xj.nexus.NexusException;

/**
 * Hook-type instrument
 * https://www.pivotaltracker.com/story/show/180416989
 */
public interface HookCraft {

  /**
   * Perform Hook-type craft for this segment
   * https://www.pivotaltracker.com/story/show/180416989
   */
  void doWork() throws NexusException;

}
