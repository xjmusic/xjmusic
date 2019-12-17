// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, Model} from "redux-orm";

class Work extends Model {
  toString() {
    return `Work: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

Work.modelName = 'Work';

// Declare your related fields.
Work.fields = {
  id: attr(),
  name: attr(),
  state: attr(),
  type: attr(),
  targetId: attr(),
};

export default Work;
