//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  segment: DS.belongsTo({}),
  sequencePattern: DS.belongsTo({}),
  transpose: DS.attr('number'),
  type: DS.attr('string'),
  sequence: DS.belongsTo({}),
  arrangements: DS.hasMany('arrangement'),
});










