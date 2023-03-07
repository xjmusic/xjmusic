// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.util.ValueException;
import org.jooq.DSLContext;

import java.util.UUID;

public interface ProgramVoiceManager extends Manager<ProgramVoice> {

  /**
   * Add a voicing to the given program
   * <p>
   * Programs persist main chord/voicing structure sensibly
   * https://www.pivotaltracker.com/story/show/182220689
   *
   * @param db        database
   * @param programId to add a voicing
   * @param type      of voicing to add
   * @return newly added voicing
   */
  ProgramVoice add(DSLContext db, UUID programId, InstrumentType type) throws ManagerException, JsonapiException, ValueException;
}
