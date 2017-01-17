// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  model: function() {
    return this.store.findAll('account').catch(function(){
      this.transitionTo('');
    });
  },

  actions: {

    edit(account) {
      this.transitionTo('admin.accounts.edit', account);
    }

  }
});
