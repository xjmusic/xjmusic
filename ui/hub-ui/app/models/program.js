//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  user: belongsTo({}),
  library: belongsTo({}),
  density: attr('number'),
  key: attr('string'),
  name: attr('string'),
  tempo: attr('number'),
  type: attr('string'),
  state: attr('string'),

  patterns: hasMany('pattern'),
  patternEvents: hasMany('event.js'),
  programMemes: hasMany('program-meme'),
  sequences: hasMany('sequence'),
  sequenceBindings: hasMany('sequence-binding'),
  sequenceBindingMemes: hasMany('sequence-binding-meme'),
  sequenceChords: hasMany('sequence-chord'),
  voices: hasMany('voice'),
  choices: hasMany('choice'),
});
