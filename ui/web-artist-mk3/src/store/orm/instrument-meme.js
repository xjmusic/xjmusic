/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import { Model, fk, attr } from "redux-orm";

class InstrumentMeme extends Model {
    toString() {
        return `InstrumentMeme: ${this.name}`;
    }
    // Declare any static or instance methods you need.
}
InstrumentMeme.modelName = 'InstrumentMeme';

// Declare your related fields.
InstrumentMeme.fields = {
    id: attr(),
  name: attr(),
  instrumentId: fk({
    to: 'Instrument',
    as: 'instrument',
    relatedName: 'memes',
  }),
};

export default InstrumentMeme;
