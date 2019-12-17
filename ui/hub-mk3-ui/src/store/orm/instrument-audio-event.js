// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class InstrumentAudioEvent extends Model {
  toString() {
    return `${this.note}(${this.name})@${this.position}`;
  }

  // Declare any static or instance methods you need.
}

InstrumentAudioEvent.modelName = 'InstrumentAudioEvent';

// Declare your related fields.
InstrumentAudioEvent.fields = {
  id: attr(),
  duration: attr(),
  name: attr(),
  note: attr(),
  position: attr(),
  velocity: attr(),
  instrumentId: fk({
    to: 'Instrument',
    as: 'instrument',
    relatedName: 'audioEvents',
  }),
  instrumentAudioId: fk({
    to: 'InstrumentAudio',
    as: 'audio',
    relatedName: 'events',
  }),
};

export default InstrumentAudioEvent;
