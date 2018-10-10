//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  instrument: DS.belongsTo({}),
  name: DS.attr('string'),
  waveformKey: DS.attr('string'),
  start: DS.attr('number'),
  length: DS.attr('number'),
  tempo: DS.attr('number'),
  pitch: DS.attr('number'),
  events: DS.hasMany('audio-event'),
  chords: DS.hasMany('audio-chord'),
});
