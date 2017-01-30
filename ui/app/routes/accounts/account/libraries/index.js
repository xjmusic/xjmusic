// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model: function() {
    let account = this.modelFor('accounts.account');
    let libraries = this.store.query('library', { account: account.get('id') }).catch((error)=>{
      Ember.get(this, 'display').error(error);
      this.transitionTo('');
    });
    return Ember.RSVP.hash({
      account: account,
      libraries: libraries,
    });
  },

  actions: {

    editLibrary(library) {
      this.transitionTo('accounts.account.libraries.library', library);
    },

  }
});
