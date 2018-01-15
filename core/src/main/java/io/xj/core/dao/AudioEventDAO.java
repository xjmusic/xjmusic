// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.audio_event.AudioEvent;

import java.math.BigInteger;
import java.util.Collection;

public interface AudioEventDAO extends DAO<AudioEvent> {

  /**
   Read all AudioEvent that are first in an audio, for all audio in an Instrument
   for each audio id, the first (in terms of position) AudioEvent

   @param access       control
   @param instrumentId to fetch audio for
   @return audios
   */
  Collection<AudioEvent> readAllFirstEventsForInstrument(Access access, BigInteger instrumentId) throws Exception;

}
