// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class Library extends Model {
  toString() {
    return `Library: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

Library.modelName = 'Library';

// Declare your related fields.
Library.fields = {
  id: attr(),
  name: attr(),
  accountId: fk({
    to: 'Account',
    as: 'account',
    relatedName: 'libraries',
  }),
  /*
    programs: many('program'),
    instruments: many('instrument'),
  */
};

export default Library;
