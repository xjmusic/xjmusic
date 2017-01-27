// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.

// import Ember from 'ember';
import account from '../services/account';

export function initialize(app) {
  app.register('service:account', account, { instantiate: true, singleton: true });
  app.inject('controller', 'account', 'service:account');
}

export default {
  name: "account",
  initialize
};
