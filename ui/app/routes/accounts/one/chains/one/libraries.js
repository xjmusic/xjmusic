// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  model() {
    let chain = this.modelFor('accounts.one.chains.one');
    let account = this.modelFor('accounts.one');
    return Ember.RSVP.hash({
      chain: chain,
      libraries: this.store.query('library', {account: account.id}),
      libraryToAdd: null,
      chainLibraries: this.store.query('chain-library', { chain: chain.id }),
    });
  },

  actions: {

    sessionChanged: function() {
      this.refresh();
    },

  },

});
