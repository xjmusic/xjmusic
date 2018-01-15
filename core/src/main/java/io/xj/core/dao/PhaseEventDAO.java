// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.phase_event.PhaseEvent;

import java.math.BigInteger;
import java.util.Collection;

public interface PhaseEventDAO extends DAO<PhaseEvent> {

  /**
   Read all events in a given voice

   @param access  control
   @param voiceId to read events for
   @return events
   */
  Collection<PhaseEvent> readAllOfVoice(Access access, BigInteger voiceId) throws Exception;

}
