// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import DS from 'ember-data';

export default DS.Model.extend({
  name: DS.attr('string'),
  accountUsers: DS.hasMany('account-user')
});
