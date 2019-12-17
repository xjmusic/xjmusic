// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class ProgramSequenceBinding extends Model {
  toString() {
    return `ProgramSequenceBinding: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

ProgramSequenceBinding.modelName = 'ProgramSequenceBinding';

// Declare your related fields.
ProgramSequenceBinding.fields = {
  id: attr(),
  offset: attr(),
  programId: fk({
    to: 'Program',
    as: 'program',
    relatedName: 'sequenceBindings',
  }),
  programSequenceId: fk({
    to: 'ProgramSequence',
    as: 'programSequence',
    relatedName: 'bindings',
  }),
  /*
    programSequenceBindingMemes: many('program-sequence-binding-meme'),
  */
};

export default ProgramSequenceBinding;
