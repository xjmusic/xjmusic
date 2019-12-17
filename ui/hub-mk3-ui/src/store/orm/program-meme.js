// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class ProgramMeme extends Model {
  toString() {
    return `ProgramMeme: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

ProgramMeme.modelName = 'ProgramMeme';

// Declare your related fields.
ProgramMeme.fields = {
  id: attr(),
  name: attr(),
  programId: fk({
    to: 'Program',
    as: 'program',
    relatedName: 'memes',
  }),
};

export default ProgramMeme;
