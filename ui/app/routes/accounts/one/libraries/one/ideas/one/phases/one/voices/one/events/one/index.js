import Ember from 'ember';

export default Ember.Route.extend({

  model: function() {
    let voice = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one');
    let event = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one.voices.one.events.one');
    return Ember.RSVP.hash({
      voice: voice,
      event: event,
    });
  },

});
