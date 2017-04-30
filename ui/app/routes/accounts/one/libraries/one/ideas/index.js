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
    let library = this.modelFor('accounts.one.libraries.one');
    let ideas = this.store.query('idea', {libraryId: library.get('id')})
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('');
      });
    return Ember.RSVP.hash({
      library: library,
      ideas: ideas,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.library.get('name') + ' ' + 'Ideas',
      entity: {
        name: 'Library',
        id: model.library.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editIdea(idea) {
      this.transitionTo('accounts.one.libraries.one.ideas.one', idea);
    },

  }

});
