// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  account: DS.belongsTo({}),
  name: DS.attr('string'),
  state: DS.attr('string'),
  type: DS.attr('string'),
  startAt: DS.attr('string'),
  stopAt: DS.attr('string'),
});

