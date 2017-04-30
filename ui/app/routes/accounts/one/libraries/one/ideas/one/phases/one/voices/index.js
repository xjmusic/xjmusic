// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
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
    let voices = this.store.query('voice', {phaseId: phase.get('id')})
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('');
      });
    return Ember.RSVP.hash({
      phase: phase,
      voices: voices,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.phase.getTitle() + ' ' + 'Voices',
      entity: {
        name: 'Phase',
        id: model.phase.get('id')
      }
    });
  },

  /**
   * Route Action
   */
  actions: {

    editVoice(voice) {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one.voices.one', voice);
    },
  }

});
