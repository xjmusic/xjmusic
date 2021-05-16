/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  type: attr('string'),
  name: attr('string'),
  programSequencePatterns: hasMany('program-sequence-pattern'),
  program: belongsTo('program'),
  programVoiceTracks: hasMany('program-voice-track'),
});
