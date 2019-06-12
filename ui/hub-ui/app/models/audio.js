//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Model, {attr, belongsTo, hasMany} from '@ember-data/model';

export default Model.extend({
  instrument: belongsTo({}),
  name: attr('string'),
  waveformKey: attr('string'),
  start: attr('number'),
  length: attr('number'),
  tempo: attr('number'),
  pitch: attr('number'),
  audioEvents: hasMany('audio-event'),
  audioChords: hasMany('audio-chord'),
});
