// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {*}
   */
  model: function () {
    let self = this;
    let phase = this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    let chords = this.store.query('phase-chord', {phaseId: phase.get('id')})
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('');
      });
    return Ember.RSVP.hash({
      phase: phase,
      chords: chords,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.phase.getTitle() + ' ' + 'Chords',
      entity: {
        name: 'Phase',
        id: model.phase.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editChord(model) {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.chords.one', model);
    },
  }

});
