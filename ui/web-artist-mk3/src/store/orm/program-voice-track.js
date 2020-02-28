/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {attr, fk, Model} from "redux-orm";

class ProgramVoiceTrack extends Model {
  toString() {
    return this.name;
  }

  // Declare any static or instance methods you need.
}

ProgramVoiceTrack.modelName = 'ProgramVoiceTrack';

// Declare your related fields.
ProgramVoiceTrack.fields = {
  id: attr(),
  name: attr(),
  programId: fk({
    to: 'Program',
    as: 'program',
    relatedName: 'voiceTracks',
  }),
  programVoiceId: fk({
    to: 'ProgramVoice',
    as: 'programVoice',
    relatedName: 'tracks',
  }),
  /*
  events: many('event'),
  */
};

export default ProgramVoiceTrack;
