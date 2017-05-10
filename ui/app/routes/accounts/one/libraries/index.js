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
    let account = this.modelFor('accounts.one');
    let libraries = this.store.query('library', {accountId: account.get('id')})
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('');
      });
    return Ember.RSVP.hash({
      account: account,
      libraries: libraries,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.account.get('name') + ' ' + 'Libraries',
      entity: {
        name: 'Account',
        id: model.account.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    editLibrary(library) {
      this.transitionTo('accounts.one.libraries.one', library);
    },

  }

});
