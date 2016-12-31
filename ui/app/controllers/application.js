// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Controller.extend({
  session: Ember.inject.service("session"),
  auth: Ember.inject.service("auth"),
  actions: {
    login() {
      window.location.replace("/auth/google");
    },
    logout() {
      // TODO: send DELETE /auth to backend
      this.get('session').invalidate();
    }
  }
});
