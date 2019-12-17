// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class SegmentChoice extends Model {
  toString() {
    return `SegmentChoice: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

SegmentChoice.modelName = 'SegmentChoice';

// Declare your related fields.
SegmentChoice.fields = {
  id: attr(),
  transpose: attr(),
  type: attr(),
  programId: fk({
    to: 'Program',
    as: 'program',
    relatedName: 'segmentChoices',
  }),
  programSequenceBindingId: fk({
    to: 'ProgramSequenceBinding',
    as: 'programSequenceBinding',
    relatedName: 'segmentChoices',
  }),
  segmentId: fk({
    to: 'Segment',
    as: 'segment',
    relatedName: 'choices',
  }),
  /*
    segmentChoiceArrangements: many('segment-choice-arrangement'),
  */
};

export default SegmentChoice;
