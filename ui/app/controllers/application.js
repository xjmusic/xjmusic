// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Controller.extend({
  session: Ember.inject.service('session'),

  actions: {
    login() {
      this.get('session').authenticate('authenticator:xj','google');
    },
    logout() {
      this.get('session').invalidate();
    }
  }
});
