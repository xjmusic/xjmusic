// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class ProgramSequenceChord extends Model {
  toString() {
    return `${this.name}@${this.position}`;
  }

  // Declare any static or instance methods you need.
}

ProgramSequenceChord.modelName = 'ProgramSequenceChord';

// Declare your related fields.
ProgramSequenceChord.fields = {
  id: attr(),
  name: attr(),
  position: attr(),
  programId: fk({
    to: 'Program',
    as: 'program',
    relatedName: 'sequenceChords',
  }),
  programSequenceId: fk({
    to: 'ProgramSequence',
    as: 'programSequence',
    relatedName: 'chords',
  }),
};

export default ProgramSequenceChord;
