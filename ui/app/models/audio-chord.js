// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  audio: DS.belongsTo({}),
  name: DS.attr('string'),
  position: DS.attr('number'),
});
