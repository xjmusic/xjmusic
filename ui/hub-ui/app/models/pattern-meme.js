// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  pattern: DS.belongsTo({}),
  name: DS.attr('string'),
});
