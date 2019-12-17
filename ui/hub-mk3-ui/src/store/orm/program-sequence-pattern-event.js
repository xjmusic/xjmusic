// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class ProgramSequencePatternEvent extends Model {
  toString() {
    return `${this.note}@${this.position}`;
  }

  // Declare any static or instance methods you need.
}

ProgramSequencePatternEvent.modelName = 'ProgramSequencePatternEvent';

// Declare your related fields.
ProgramSequencePatternEvent.fields = {
  id: attr(),
  duration: attr(),
  note: attr(),
  position: attr(),
  velocity: attr(),
  programVoiceTrackId: fk({
    to: 'ProgramVoiceTrack',
    as: 'programVoiceTrack',
    relatedName: 'events',
  }),
  programSequencePatternId: fk({
    to: 'ProgramSequencePattern',
    as: 'programSequencePattern',
    relatedName: 'events',
  }),
  programId: fk({
    to: 'Program',
    as: 'program',
    relatedName: 'sequencePatternEvents',
  }),
};

export default ProgramSequencePatternEvent;
