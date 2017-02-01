// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  model() {
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    return Ember.RSVP.hash({
      idea: idea,
      phaseToAdd: null,
      ideaPhases: this.store.query('phase', { idea: idea.id }),
    });
  },

  actions: {

    sessionChanged: function() {
      this.refresh();
    },

  },

});
