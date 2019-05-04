// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.audio_event.AudioEvent;

import java.math.BigInteger;
import java.util.Collection;

public interface AudioEventDAO extends DAO<AudioEvent> {

  /**
   [#161197150] Developer wants to request all audioEvent for a specified instrument id, for efficiency loading an entire instrument.

   @param access        control
   @param instrumentIds to fetch audio for
   @return audios
   */
  Collection<AudioEvent> readAllOfInstrument(Access access, ImmutableList<BigInteger> instrumentIds) throws CoreException;
}
