// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class SegmentMessage extends Model {
  toString() {
    return `[${this.type}] ${this.body}`;
  }

  // Declare any static or instance methods you need.
}

SegmentMessage.modelName = 'SegmentMessage';

// Declare your related fields.
SegmentMessage.fields = {
  id: attr(),
  body: attr(),
  type: attr(),
  segmentId: fk({
    to: 'Segment',
    as: 'segment',
    relatedName: 'segmentMessages',
  }),
};

export default SegmentMessage;
