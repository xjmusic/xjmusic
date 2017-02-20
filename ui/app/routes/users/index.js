// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model: function() {
    return this.store.findAll('user').catch((error)=>{
      Ember.get(this, 'display').error(error);
      this.transitionTo('');
    });
  },

  actions: {

    editUser(user) {
      this.transitionTo('users.one', user);
    },

  },

});
