// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  phase: DS.belongsTo({}),
  type: DS.attr('string'),
  description: DS.attr('string'),
});
