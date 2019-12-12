// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  density: attr('number'),
  key: attr('string'),
  name: attr('string'),
  tempo: attr('number'),

  library: belongsTo('library'),
  program: belongsTo('program'),
  sequenceBindings: hasMany('program-sequence-binding'),
  sequenceChords: hasMany('program-sequence-chord'),
  voices: hasMany('program-voice'),
  patterns: hasMany('program-sequence-pattern'),
  choices: hasMany('segment-choice'),
});
