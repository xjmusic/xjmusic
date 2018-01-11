// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  link: DS.belongsTo({}),
  phaseOffset: DS.attr('number'),
  transpose: DS.attr('number'),
  type: DS.attr('string'),
  pattern: DS.belongsTo({}),
  arrangements: DS.hasMany('arrangement'),
});










