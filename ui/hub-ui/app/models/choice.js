// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  link: DS.belongsTo({}),
  phaseOffset: DS.attr('number'),
  transpose: DS.attr('number'),
  type: DS.attr('string'),
  pattern: DS.belongsTo({})

});










