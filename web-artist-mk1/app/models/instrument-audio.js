/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  name: attr('string'),
  waveformKey: attr('string'),
  start: attr('number'),
  length: attr('number'),
  tempo: attr('number'),
  instrument: belongsTo('instrument'),
  instrumentAudioEvents: hasMany('instrument-audio-event'),
  instrumentAudioChords: hasMany('instrument-audio-chord'),
});
