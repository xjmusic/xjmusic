// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model() {
    return this.modelFor('accounts.account');
  },

  actions: {

    saveAccount(model) {
      model.save().then(() => {
        Ember.get(this, 'display').success('Updated account ' + model.get('name') + '.');
        this.transitionTo('accounts');
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

    destroyAccount(model) {
      let confirmation = confirm("Are you fucking sure? If there are Users or Libraries belonging to this account, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord().then(() => {
          Ember.get(this, 'display').success('Deleted account ' + model.get('name') + '.');
          this.transitionTo('accounts');
        }).catch((error) => {
          Ember.get(this, 'display').error(error);
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
