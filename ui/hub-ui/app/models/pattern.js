// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  library: DS.belongsTo({}),
  user: DS.belongsTo({}),
  density: DS.attr('number'),
  key: DS.attr('string'),
  name: DS.attr('string'),
  tempo: DS.attr('number'),
  type: DS.attr('string'),

  memes: DS.hasMany('pattern-meme'),
  voices: DS.hasMany('voice'),
  phases: DS.hasMany('phase'),
  choices: DS.hasMany('choice'),
});
