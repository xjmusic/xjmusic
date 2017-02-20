// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  library: DS.belongsTo({}),
  user: DS.belongsTo({}),
  density: DS.attr('number'),
  description: DS.attr('string'),
  type: DS.attr('string'),
});
