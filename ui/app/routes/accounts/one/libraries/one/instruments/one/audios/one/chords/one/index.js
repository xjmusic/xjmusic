import Ember from 'ember';

export default Ember.Route.extend({

  model: function() {
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    let chord = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one.chords.one');
    return Ember.RSVP.hash({
      audio: audio,
      chord: chord,
    });
  },

});
