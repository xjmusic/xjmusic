// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.audio_event.AudioEvent;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Collection;

public interface AudioEventDAO {

  /**
   Create a new AudioEvent

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  AudioEvent create(Access access, AudioEvent entity) throws Exception;

  /**
   Fetch one Audio Event if accessible

   @param access control
   @param id     of audio
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  AudioEvent readOne(Access access, BigInteger id) throws Exception;

  /**
   Fetch all accessible Audio Event for one Audio by id

   @param access  control
   @param audioId to fetch audios for.
   @return JSONArray of audios.
   @throws Exception on failure
   */
  Collection<AudioEvent> readAll(Access access, BigInteger audioId) throws Exception;

  /**
   Read all AudioEvent that are first in an audio, for all audio in an Instrument
   for each audio id, the first (in terms of position) AudioEvent

   @param access       control
   @param instrumentId to fetch audio for
   @return audios
   */
  Collection<AudioEvent> readAllFirstEventsForInstrument(Access access, BigInteger instrumentId) throws Exception;

  /**
   Update a specified Audio Event if accessible

   @param access control
   @param id     of specific Event to update.
   @param entity for the updated Event.
   */
  void update(Access access, BigInteger id, AudioEvent entity) throws Exception;

  /**
   Delete a specified Audio Event if accessible

   @param access control
   @param id     of specific audio to delete.
   */
  void delete(Access access, BigInteger id) throws Exception;
}
