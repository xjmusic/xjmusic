// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.access.impl.Access;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.tables.records.AudioEventRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

import javax.annotation.Nullable;
import java.util.List;

public interface AudioEventDAO {

  /**
   Create a new AudioEvent

   @param access control
   @param entity for the new Account User.
   @return newly readMany record
   */
  AudioEventRecord create(Access access, AudioEvent entity) throws Exception;

  /**
   Fetch one Audio Event if accessible

   @param access control
   @param id     of audio
   @return retrieved record
   @throws Exception on failure
   */
  @Nullable
  AudioEventRecord readOne(Access access, ULong id) throws Exception;

  /**
   Fetch all accessible Audio Event for one Audio by id

   @param access  control
   @param audioId to fetch audios for.
   @return JSONArray of audios.
   @throws Exception on failure
   */
  Result<AudioEventRecord> readAll(Access access, ULong audioId) throws Exception;

  /**
   Read all AudioEvent that are first in an audio, for all audio in an Instrument
   for each audio id, the first (in terms of position) AudioEvent

   @return audios
    @param access       control
   @param instrumentId to fetch audio for
   */
  List<AudioEvent> readAllFirstEventsForInstrument(Access access, ULong instrumentId) throws Exception;

  /**
   Update a specified Audio Event if accessible

   @param access control
   @param id     of specific Event to update.
   @param entity for the updated Event.
   */
  void update(Access access, ULong id, AudioEvent entity) throws Exception;

  /**
   Delete a specified Audio Event if accessible

   @param access control
   @param id     of specific audio to delete.
   */
  void delete(Access access, ULong id) throws Exception;
}
