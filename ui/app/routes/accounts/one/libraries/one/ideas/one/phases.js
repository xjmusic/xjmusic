// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model() {
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    return Ember.RSVP.hash({
      idea: idea,
      phaseToAdd: null,
      ideaPhases: this.store.query('phase', { ideaId: idea.id }),
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

    sessionChanged: function() {
      this.refresh();
    },

  },

});
