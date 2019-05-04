// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.pattern_event.PatternEvent;

import java.math.BigInteger;
import java.util.Collection;

public interface PatternEventDAO extends DAO<PatternEvent> {

  /**
   Read all events in a given voice

   @param access  control
   @param voiceId to read events for
   @return events
   */
  Collection<PatternEvent> readAllOfVoice(Access access, BigInteger voiceId) throws CoreException;

}
