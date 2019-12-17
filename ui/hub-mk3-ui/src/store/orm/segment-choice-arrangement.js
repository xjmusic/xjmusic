// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class SegmentChoiceArrangement extends Model {
  toString() {
    return `SegmentChoiceArrangement: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

SegmentChoiceArrangement.modelName = 'SegmentChoiceArrangement';

// Declare your related fields.
SegmentChoiceArrangement.fields = {
  id: attr(),
  segmentId: fk({
    to: 'Segment',
    as: 'segment',
    relatedName: 'choiceArrangements',
  }),
  segmentChoiceId: fk({
    to: 'SegmentChoice',
    as: 'segmentChoice',
    relatedName: 'arrangements',
  }),
  programVoiceId: fk({
    to: 'ProgramVoice',
    as: 'programVoice',
    relatedName: 'segmentChoiceArrangements',
  }),
  instrumentId: fk({
    to: 'Instrument',
    as: 'instrument',
    relatedName: 'segmentChoiceArrangements',
  }),
};

export default SegmentChoiceArrangement;
