import Ember from 'ember';

export default Ember.Route.extend({

  model(params) {
    return this.store.findRecord('idea', params.idea_id)
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('accounts.one.libraries.one.ideas');
      });
  },

  afterModel(model) {
    Ember.set(this, 'breadCrumb', model);
  }

});
