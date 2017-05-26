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
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    let phases = this.store.query('phase', {ideaId: idea.get('id')})
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('');
      });
    return Ember.RSVP.hash({
      idea: idea,
      phases: phases,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.idea.get('name') + ' ' + 'Phases',
      entity: {
        name: 'Idea',
        id: model.idea.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editPhase(model) {
      this.transitionTo('accounts.one.libraries.one.ideas.one.phases.one', model);
    },
  }

});
