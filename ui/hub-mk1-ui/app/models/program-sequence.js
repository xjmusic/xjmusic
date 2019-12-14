// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  density: attr('number'),
  key: attr('string'),
  name: attr('string'),
  tempo: attr('number'),

  library: belongsTo('library'),
  program: belongsTo('program'),
  programSequenceBindings: hasMany('program-sequence-binding'),
  programSequenceChords: hasMany('program-sequence-chord'),
  programVoices: hasMany('program-voice'),
  programSequencePatterns: hasMany('program-sequence-pattern'),
  segmentChoices: hasMany('segment-choice'),
});
