/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {attr, fk, Model} from "redux-orm";

class SegmentMeme extends Model {
  toString() {
    return `${this.name}@${this.position}`;
  }

  // Declare any static or instance methods you need.
}

SegmentMeme.modelName = 'SegmentMeme';

// Declare your related fields.
SegmentMeme.fields = {
  id: attr(),
  name: attr(),
  segmentId: fk({
    to: 'Segment',
    as: 'segment',
    relatedName: 'segmentMemes',
  }),
};

export default SegmentMeme;
