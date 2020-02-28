/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  density: attr('number'),
  key: attr('string'),
  name: attr('string'),
  tempo: attr('number'),
  type: attr('string'),
  state: attr('string'),

  user: belongsTo('user'),
  library: belongsTo('library'),
  programSequencePatterns: hasMany('program-sequence-pattern'),
  programSequencePatternEvents: hasMany('program-sequence-pattern-event'),
  programMemes: hasMany('program-meme'),
  programSequences: hasMany('program-sequence'),
  programSequenceBindings: hasMany('program-sequence-binding'),
  programSequenceBindingMemes: hasMany('program-sequence-binding-meme'),
  programSequenceChords: hasMany('program-sequence-chord'),
  programVoices: hasMany('program-voice'),
  choices: hasMany('segment-choice'),
});
