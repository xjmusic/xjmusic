// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  pattern: DS.belongsTo({}),
  type: DS.attr('string'),
  description: DS.attr('string'),
  arrangements: DS.hasMany('arrangement'),
  events: DS.hasMany('voice-event')

});
