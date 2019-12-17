// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class InstrumentAudio extends Model {
  toString() {
    return `InstrumentAudio: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

InstrumentAudio.modelName = 'InstrumentAudio';

// Declare your related fields.
InstrumentAudio.fields = {
  id: attr(),
  name: attr(),
  waveformKey: attr(),
  start: attr(),
  length: attr(),
  tempo: attr(),
  pitch: attr(),
  instrumentId: fk({
    to: 'Instrument',
    as: 'instrument',
    relatedName: 'audios',
  }),
  /*
    events: many('instrument-audio-event'),
    chords: many('instrument-audio-chord'),
  */
};

export default InstrumentAudio;
