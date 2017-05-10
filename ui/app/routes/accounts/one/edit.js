// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {*|DS.Model}
   */
  model() {
    return this.modelFor('accounts.one');
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: 'Edit ' + model.get('name'),
      entity: {
        name: 'Account',
        id: model.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    saveAccount(model) {
      let self = this;
      model.save().then(
        () => {
          Ember.get(self, 'display').success('Updated account ' + model.get('name') + '.');
          self.transitionTo('accounts.one');
        },
        (error) => {
          Ember.get(self, 'display').error(error);
        }
      );
    },

    destroyAccount(model) {
      let self = this;
      let confirmation = confirm("Are you fucking sure? If there are Users or Libraries belonging to this account, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord({}).then(
          () => {
            Ember.get(self, 'display').success('Deleted account ' + model.get('name') + '.');
            self.transitionTo('accounts');
          },
          (error) => {
            Ember.get(self, 'display').error(error);
          });
      }
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
