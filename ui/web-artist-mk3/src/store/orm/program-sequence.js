/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {attr, fk, Model} from "redux-orm";

class ProgramSequence extends Model {
  toString() {
    return `ProgramSequence: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

ProgramSequence.modelName = 'ProgramSequence';

// Declare your related fields.
ProgramSequence.fields = {
  id: attr(),
  density: attr(),
  key: attr(),
  name: attr(),
  tempo: attr(),
  total: attr(),
  programId: fk({
    to: 'Program',
    as: 'program',
    relatedName: 'sequences',
  }),
  /*
  programSequenceBindings: many('program-sequence-binding'),
  programSequenceChords: many('program-sequence-chord'),
  programVoices: many('program-voice'),
  programSequencePatterns: many('program-sequence-pattern'),
  segmentChoices: many('segment-choice'),
   */
};

export default ProgramSequence;
