// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class SegmentChord extends Model {
  toString() {
    return `${this.name}@${this.position}`;
  }

  // Declare any static or instance methods you need.
}

SegmentChord.modelName = 'SegmentChord';

// Declare your related fields.
SegmentChord.fields = {
  id: attr(),
  name: attr(),
  position: attr(),
  segmentId: fk({
    to: 'Segment',
    as: 'segment',
    relatedName: 'segmentChords',
  }),
};

export default SegmentChord;
