// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Controller.extend({
  auth: Ember.inject.service(),
  account: Ember.inject.service(),
  needs: ['application'],

  actions: {

    openAccountSelector() {
      // TODO: Launch account-selector modal component
      this.transitionToRoute('access.accounts');
    },

  }
});
