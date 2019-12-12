// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  transpose: attr('number'),
  type: attr('string'),
  program: belongsTo('program'),
  programSequenceBinding: belongsTo('program-sequence-binding'),
  segment: belongsTo('segment'),
  segmentChoiceArrangements: hasMany('segment-choice-arrangement'),
});










