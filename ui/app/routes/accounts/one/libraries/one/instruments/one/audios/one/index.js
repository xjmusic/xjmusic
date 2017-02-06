import Ember from 'ember';

export default Ember.Route.extend({

  model: function() {
    let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    return Ember.RSVP.hash({
      instrument: instrument,
      audio: audio,
    });
  },

});
