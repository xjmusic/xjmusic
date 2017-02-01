// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model: function() {
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    let phases = this.store.query('phase', { idea: idea.get('id') }).catch((error)=>{
      Ember.get(this, 'display').error(error);
      this.transitionTo('');
    });
    return Ember.RSVP.hash({
      idea: idea,
      phases: phases,
    });
  },

  actions: {

    editPhase(phase) {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one', phase);
    },

  }
});
