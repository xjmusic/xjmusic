// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  account: Ember.inject.service(),

  display: Ember.inject.service(),

  model: function() {
    let account = this.get('account').get('current');
    if (account==null) {
      this.transitionTo('');
      return null;
    }
    return this.store.query('library', { account: account.get('id') }).catch((error)=>{
      Ember.get(this, 'display').error(error);
      this.transitionTo('');
    });
  },

  actions: {

    editLibrary(library) {
      this.transitionTo('library.edit', library);
    },

    selectLibrary(library) {
      Ember.get(this, 'library').switchTo(library);
    }

  }
});
