import Ember from 'ember';

export default Ember.Route.extend({

  model: function() {
    let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    let voice = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one');
    return Ember.RSVP.hash({
      phase: phase,
      voice: voice,
    });
  },

});
