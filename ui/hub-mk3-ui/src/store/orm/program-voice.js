// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class ProgramVoice extends Model {
  toString() {
    return `ProgramVoice: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

ProgramVoice.modelName = 'ProgramVoice';

// Declare your related fields.
ProgramVoice.fields = {
  id: attr(),
  density: attr(),
  key: attr(),
  name: attr(),
  tempo: attr(),
  programId: fk({
    to: 'Program',
    as: 'program',
    relatedName: 'voices',
  }),
  /*
  segmentChoiceArrangements: many('segment-choice-arrangement'),
  programSequencePatterns: many('program-sequence-pattern'),
   */
};

export default ProgramVoice;
