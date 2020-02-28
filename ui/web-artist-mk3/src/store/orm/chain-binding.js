/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {attr, fk, Model} from "redux-orm";

class ChainBinding extends Model {
  toString() {
    return `ChainBinding: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

ChainBinding.modelName = 'ChainBinding';

// Declare your related fields.
ChainBinding.fields = {
  id: attr(),
  type: attr(),
  targetId: attr(),
  chainId: fk({
    to: 'Chain',
    as: 'chain',
    relatedName: 'chainBindings',
  }),
};

export default ChainBinding;
