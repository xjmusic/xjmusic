// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  name: DS.attr('string'),
  avatarUrl: DS.attr('string'),
  email: DS.attr('string'),
  roles: DS.attr('string')
});
