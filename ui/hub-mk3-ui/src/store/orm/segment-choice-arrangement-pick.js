// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import {attr, fk, Model} from "redux-orm";

class SegmentChoiceArrangementPick extends Model {
  toString() {
    return `SegmentChoiceArrangementPick: ${this.name}`;
  }

  // Declare any static or instance methods you need.
}

SegmentChoiceArrangementPick.modelName = 'SegmentChoiceArrangementPick';

// Declare your related fields.
SegmentChoiceArrangementPick.fields = {
  id: attr(),
  start: attr(),
  length: attr(),
  amplitude: attr(),
  pitch: attr(),
  name: attr(),
  segmentChoiceArrangementId: fk({
    to: 'SegmentChoiceArrangement',
    as: 'segmentChoiceArrangement',
    relatedName: 'segmentChoiceArrangementPicks',
  }),
  instrumentAudioId: fk({
    to: 'InstrumentAudio',
    as: 'instrumentAudio',
    relatedName: 'segmentChoiceArrangementPicks',
  }),
  programSequencePatternEventId: fk({
    to: 'ProgramSequencePatternEvent',
    as: 'programSequencePatternEvent',
    relatedName: 'segmentChoiceArrangementPicks',
  }),
  segmentId: fk({
    to: 'Segment',
    as: 'segment',
    relatedName: 'segmentChoiceArrangementPicks',
  }),
  programVoiceId: fk({
    to: 'ProgramVoice',
    as: 'programVoice',
    relatedName: 'segmentChoiceArrangementPicks',
  }),
};

export default SegmentChoiceArrangementPick;
