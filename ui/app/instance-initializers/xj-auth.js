// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

// import Ember from 'ember';
import xjAuth from '../services/xj-auth';

export function initialize(app) {
  app.register('service:xj-auth', xjAuth, { instantiate: true, singleton: true });
  app.inject('controller', 'auth', 'service:xj-auth');
}

export default {
  name: "authorizer",
  initialize
};
