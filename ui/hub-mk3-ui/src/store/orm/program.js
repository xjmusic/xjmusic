// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class Program extends Model {
  toString() {
    return `Program: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

Program.modelName = 'Program';

// Declare your related fields.
Program.fields = {
  id: attr(),
  name: attr(),
  type: attr(),
  state: attr(),
  key: attr(),
  tempo: attr(),
  density: attr(),
  userId: fk({
    to: 'User',
    as: 'user',
    relatedName: 'programs',
  }),
  libraryId: fk({
    to: 'Library',
    as: 'library',
    relatedName: 'programs',
  }),
  /*
    programSequencePatterns: many('program-sequence-pattern'),
    programSequencePatternEvents: many('program-sequence-pattern-event'),
    programMemes: many('program-meme'),
    programSequences: many('program-sequence'),
    programSequenceBindings: many('program-sequence-binding'),
    programSequenceBindingMemes: many('program-sequence-binding-meme'),
    programSequenceChords: many('program-sequence-chord'),
    programVoices: many('program-voice'),
    choices: many('segment-choice'),
  */
};

export default Program;
