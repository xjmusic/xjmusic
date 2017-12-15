// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  phase: DS.belongsTo({}),
  type: DS.attr('string'),
  description: DS.attr('string'),
  arrangements: DS.hasMany('arrangement'),
  pattern: DS.belongsTo({}),
});
