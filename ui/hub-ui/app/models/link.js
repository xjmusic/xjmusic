// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  chain: DS.belongsTo({}),
  offset: DS.attr('number'),
  state: DS.attr('string'),
  beginAt: DS.attr('string'),
  endAt: DS.attr('string'),
  total: DS.attr('number'),
  density: DS.attr('number'),
  key: DS.attr('string'),
  tempo: DS.attr('number'),
  waveformKey: DS.attr('string'),

  messages: DS.hasMany('link-message'),
  memes: DS.hasMany('link-meme'),
  choices: DS.hasMany('choice'),
  chords: DS.hasMany('link-chord'),

  getTitle() {
    return 'Link' +
      '@' + this.get("offset");
  }

});

