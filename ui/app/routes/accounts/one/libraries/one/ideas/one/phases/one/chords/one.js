import Ember from 'ember';

export default Ember.Route.extend({
  model(params) {
    return this.store.findRecord('phase-chord', params.chord_id)
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.chords');
      });
  },
});
