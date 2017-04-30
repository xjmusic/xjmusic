// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let idea = this.modelFor('accounts.one.libraries.one.ideas.one');
    let phase =this.modelFor('accounts.one.libraries.one.ideas.one.phases.one');
    return Ember.RSVP.hash({
      idea: idea,
      phase: phase,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.phase.getTitle(),
      entity: {
        name: 'Phase',
        id: model.phase.get('id')
      }
    });
  },

});
