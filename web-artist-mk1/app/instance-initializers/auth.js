/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */

// import Ember from 'ember';
import auth from '../services/auth';

export function initialize(app) {
  app.register('service:auth', auth, {instantiate: true, singleton: true});
  app.inject('controller', 'auth', 'service:auth');
}

export default {
  name: "auth",
  initialize
};
