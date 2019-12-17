// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class ProgramSequencePattern extends Model {
  toString() {
    let name = this.name;
    let title = name ? name : '';
    return `${title}@${this.offset}`;
  }

  // Declare any static or instance methods you need.
}

ProgramSequencePattern.modelName = 'ProgramSequencePattern';

// Declare your related fields.
ProgramSequencePattern.fields = {
  id: attr(),
  density: attr(),
  type: attr(),
  name: attr(),
  total: attr(),
  programId: fk({
    to: 'Program',
    as: 'program',
    relatedName: 'sequencePatterns',
  }),
  programVoiceId: fk({
    to: 'ProgramVoice',
    as: 'programVoice',
    relatedName: 'patterns',
  }),
  programSequenceId: fk({
    to: 'ProgramSequence',
    as: 'programSequence',
    relatedName: 'patterns',
  }),
  /*
  programSequencePatternEvents: many('program-sequence-pattern-event'),
  */
};

export default ProgramSequencePattern;
