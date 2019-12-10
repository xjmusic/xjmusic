// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  library: belongsTo({}),
  program: belongsTo({}),
  density: attr('number'),
  key: attr('string'),
  name: attr('string'),
  tempo: attr('number'),

  sequenceBindings: hasMany('sequence-binding'),
  sequenceChords: hasMany('sequence-chord'),
  voices: hasMany('voice'),
  patterns: hasMany('pattern'),
  choices: hasMany('choice'),
});
