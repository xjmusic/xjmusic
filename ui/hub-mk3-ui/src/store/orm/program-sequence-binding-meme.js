// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class ProgramSequenceBindingMeme extends Model {
  toString() {
    return `ProgramSequenceBindingMeme: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

ProgramSequenceBindingMeme.modelName = 'ProgramSequenceBindingMeme';

// Declare your related fields.
ProgramSequenceBindingMeme.fields = {
  id: attr(),
  name: attr(),
  programId: fk({
    to: 'Program',
    as: 'program',
    relatedName: 'sequenceBindingMemes',
  }),
  programSequenceBindingId: fk({
    to: 'ProgramSequenceBinding',
    as: 'programSequenceBinding',
    relatedName: 'memes',
  }),
};

export default ProgramSequenceBindingMeme;
