// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Controller.extend({
  auth: Ember.inject.service("auth"),
  actions: {
    login() {
      window.location.replace("/auth/google");
    },
    logout() {
      window.location.replace("/auth/no");
    }
  }
});
