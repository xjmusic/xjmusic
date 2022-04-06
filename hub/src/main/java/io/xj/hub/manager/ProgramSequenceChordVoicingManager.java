// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProgramSequenceChordVoicingManager extends Manager<ProgramSequenceChordVoicing> {
  /**
   Read all voicings for chords
   <p>
   Chord Search while composing a main program
   https://www.pivotaltracker.com/story/show/178921705

   @param access   control
   @param chordIds for which to retrieve voicings
   @return voicings for chords
   */
  Collection<ProgramSequenceChordVoicing> readManyForChords(HubAccess access, List<UUID> chordIds) throws ManagerException;
}
