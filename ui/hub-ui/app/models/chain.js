//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from "ember-data";

export default DS.Model.extend({
  account: DS.belongsTo({}),
  name: DS.attr('string'),
  state: DS.attr('string'),
  type: DS.attr('string'),
  startAt: DS.attr('string',{defaultValue:'now'}),
  stopAt: DS.attr('string',{defaultValue:''}),
  embedKey: DS.attr('string',{defaultValue:''}),

  segments: DS.hasMany('segment'),
  configs: DS.hasMany('chain-config'),
  libraries: DS.hasMany('chain-library'),
  instruments: DS.hasMany('chain-instrument'),
  sequences: DS.hasMany('chain-sequence')
});

