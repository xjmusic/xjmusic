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
    let library = this.modelFor('accounts.one.libraries.one');
    let instruments = this.store.query('instrument', {libraryId: library.get('id')})
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('');
      });
    return Ember.RSVP.hash({
      library: library,
      instruments: instruments,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.library.get('name') + ' ' + 'Instruments',
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

    editInstrument(model) {
      this.transitionTo('accounts.one.libraries.one.instruments.one', model);
    },

  }

});
