// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {*|DS.Model|Promise}
   */
  model: function () {
    let account = this.modelFor('accounts.one');
    return this.store.createRecord('library', {
      account: account
    });
  },

  /**
   * Headline
   */
  afterModel() {
    let account = this.modelFor('accounts.one');
    Ember.set(this, 'routeHeadline', {
      title: 'New Library',
      entity: {
        name: 'Account',
        id: account.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    createLibrary(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Created library ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one', model);
        }, (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    willTransition(transition) {
      let model = this.controller.get('model');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
        } else {
          transition.abort();
        }
      }
    }

  }
});
