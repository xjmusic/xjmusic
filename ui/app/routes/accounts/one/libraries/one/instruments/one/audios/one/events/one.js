import Ember from 'ember';

export default Ember.Route.extend({

  model(params) {
    return this.store.findRecord('audio_event', params.event_id)
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one.events');
      });
  },

  afterModel(model) {
    Ember.set(this, 'breadCrumb', model);
  }

});
