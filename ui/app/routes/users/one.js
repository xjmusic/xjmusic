// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @param params
   * @returns {Promise.<T>}
   */
  model(params) {
    let self = this;
    return this.store.findRecord('user', params.user_id)
      .catch((error) => {
        Ember.get(self, 'display').error(error);
        self.transitionTo('users');
      });
  },

  /**
   * Breadcrumb & Headline
   * @param model
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.get('name'),
      detail: {
        roles: model.get('roles').split(',')
      },
      entity: {
        name: 'User',
        id: model.get('id')
      }
    });
    Ember.set(this, 'breadCrumb', {
      title: model.get("name")
    });
  },

  /**
   * Route Actions
   */
  actions: {

    saveUser(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Updated user ' + model.get('name') + '.');
          this.transitionTo('users');
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
    },

  },

});
