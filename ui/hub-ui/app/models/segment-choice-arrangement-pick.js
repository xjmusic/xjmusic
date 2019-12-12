// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo} from '@ember-data/model';

export default Model.extend({
  start: attr('number'),
  length: attr('number'),
  amplitude: attr('number'),
  pitch: attr('number'),
  name: attr('string'),
  segmentChoiceArrangement: belongsTo('segment-choice-arrangement'),
  instrumentAudio: belongsTo('instrument-audio'),
  programSequencePatternEvent: belongsTo('program-sequence-pattern-event'),
  segment: belongsTo('segment'),
  programVoice: belongsTo('program-voice'),
});










