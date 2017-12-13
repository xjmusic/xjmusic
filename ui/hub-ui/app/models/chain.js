// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from "ember-data";

export default DS.Model.extend({
  account: DS.belongsTo({}),
  name: DS.attr('string'),
  state: DS.attr('string'),
  type: DS.attr('string'),
  startAt: DS.attr('string',{defaultValue:'now'}),
  stopAt: DS.attr('string',{defaultValue:''}),
  embedKey: DS.attr('string',{defaultValue:''}),

  links: DS.hasMany('link'),
  configs: DS.hasMany('chain-config'),
  libraries: DS.hasMany('chain-library'),
  instruments: DS.hasMany('chain-instrument'),
  patterns: DS.hasMany('chain-pattern')
});

