import Ember from 'ember';

export default Ember.Route.extend({

  model(params) {
    return this.store.findRecord('audio_chord', params.chord_id)
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.chords');
      });
  },

  afterModel(model) {
    Ember.set(this, 'breadCrumb', model);
  }

});
