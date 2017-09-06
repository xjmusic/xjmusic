// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  account: DS.belongsTo({}),
  name: DS.attr('string'),

  ideas: DS.hasMany('idea'),
  instruments: DS.hasMany('instrument'),
});

