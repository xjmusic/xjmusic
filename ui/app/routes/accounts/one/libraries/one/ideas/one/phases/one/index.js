import Ember from 'ember';

export default Ember.Route.extend({

  model: function() {
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    let phase =this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    return Ember.RSVP.hash({
      idea: idea,
      phase: phase,
    });
  },

});
