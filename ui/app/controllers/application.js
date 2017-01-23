// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Controller.extend({
  auth: Ember.inject.service("auth"),
  needs: ['application'],
  isRouteAccess: Ember.computed.match('currentRouteName', /^access/),
  isRouteYo: Ember.computed.match('currentRouteName', /^yo/)
});
