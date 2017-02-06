import Ember from 'ember';

export default Ember.Route.extend({

  model(params) {
    return this.store.findRecord('voice', params.voice_id)
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices');
      });
  },

  afterModel(model) {
    Ember.set(this, 'breadCrumb', model);
  }

});
