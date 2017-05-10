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
    return this.store.createRecord('account');
  },

  /**
   * Headline
   */
  afterModel() {
    Ember.set(this, 'routeHeadline', {
      title: 'New Account'
    });
  },

  /**
   * Route Actions
   */
  actions: {

    createAccount(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Created account ' + model.get('name') + '.');
          this.transitionTo('accounts.one', model);
        },
        (error) => {
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
