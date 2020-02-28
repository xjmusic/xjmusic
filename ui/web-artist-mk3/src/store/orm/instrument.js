/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {attr, fk, Model} from "redux-orm";

class Instrument extends Model {
  toString() {
    return `Instrument: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

Instrument.modelName = 'Instrument';

// Declare your related fields.
Instrument.fields = {
  id: attr(),
  density: attr(),
  name: attr(),
  type: attr(),
  state: attr(),
  userId: fk({
    to: 'User',
    as: 'user',
    relatedName: 'instruments',
  }),
  libraryId: fk({
    to: 'Library',
    as: 'library',
    relatedName: 'instruments',
  }),
  /*
    memes: many('instrument-meme'),
    audios: many('instrument-audio'),
    audioChords: many('instrument-audio-chord'),
    audioEvents: many('instrument-audio-event'),
    segmentChoiceArrangements: many('segment-choice-arrangement'),
  */
};

export default Instrument;
