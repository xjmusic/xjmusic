/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  density: attr('number'),
  name: attr('string'),
  config: attr('string'),
  type: attr('string'),
  state: attr('string'),
  library: belongsTo('library'),
  instrumentMemes: hasMany('instrument-meme'),
  instrumentAudios: hasMany('instrument-audio'),
  instrumentAudioChords: hasMany('instrument-audio-chord'),
  instrumentAudioEvents: hasMany('instrument-audio-event'),
});
