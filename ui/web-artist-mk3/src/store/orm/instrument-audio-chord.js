/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {attr, fk, Model} from "redux-orm";

class InstrumentAudioChord extends Model {
  toString() {
    return `${this.name}@${this.position}`;
  }

  // Declare any static or instance methods you need.
}

InstrumentAudioChord.modelName = 'InstrumentAudioChord';

// Declare your related fields.
InstrumentAudioChord.fields = {
  id: attr(),
  name: attr(),
  position: attr(),
  instrumentId: fk({
    to: 'Instrument',
    as: 'instrument',
    relatedName: 'audioChords',
  }),
  instrumentAudioId: fk({
    to: 'InstrumentAudio',
    as: 'audio',
    relatedName: 'chords',
  }),
};

export default InstrumentAudioChord;
