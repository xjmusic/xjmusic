//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  library: DS.belongsTo({}),
  user: DS.belongsTo({}),
  density: DS.attr('number'),
  key: DS.attr('string'),
  name: DS.attr('string'),
  tempo: DS.attr('number'),
  type: DS.attr('string'),

  sequencePatterns: DS.hasMany('sequence-pattern'),
  memes: DS.hasMany('sequence-meme'),
  voices: DS.hasMany('voice'),
  patterns: DS.hasMany('pattern'),
  choices: DS.hasMany('choice'),
});
