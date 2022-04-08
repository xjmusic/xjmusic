// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.manager;

import io.xj.hub.access.HubAccess;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.ProgramSequenceChord;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ProgramSequenceChordManager extends Manager<ProgramSequenceChord> {
  /**
   Chord Search while composing a main program
   https://www.pivotaltracker.com/story/show/178921705

   @param access    control
   @param libraryId within which to search
   @param chordName for which to search
   @return chord voicings
   @throws ManagerException on failure, or if chord name is empty
   */
  Collection<ProgramSequenceChord> search(HubAccess access, UUID libraryId, String chordName) throws ManagerException;

  /**
   Clone an existing program sequence chord's voicings
   https://www.pivotaltracker.com/story/show/178921705

   @param access       control
   @param cloneId      of chord to clone
   @param entity       for the new chord
   @param voicingTypes to clone voicings along with chord
   @return newly readMany record
   */
  ManagerCloner<ProgramSequenceChord> clone(HubAccess access, UUID cloneId, ProgramSequenceChord entity, List<InstrumentType> voicingTypes) throws ManagerException;
}
