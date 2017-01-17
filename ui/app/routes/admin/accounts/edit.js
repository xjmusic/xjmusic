// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  flashMessages: Ember.inject.service(),

  model(params) {
    return this.store.findRecord('account', params.account_id).catch(function(){
      this.transitionTo('accounts');
    });
  },

  actions: {

    updateAccount(model) {
      model.save().then(() => {
        Ember.get(this, 'flashMessages').success('Account updated.');
        this.transitionTo('admin.accounts');
      }).catch((adapterError) => {
        Ember.get(this, 'flashMessages').danger('Error: ' + adapterError.errors[0].detail);
      });
    },

    deleteAccount(model) {
      let confirmation = confirm("Are you fucking sure? If there are users or libraries belonging to this account, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord().then(() => {
          Ember.get(this, 'flashMessages').success('Account deleted.');
          this.transitionTo('admin.accounts');
        }).catch((adapterError) => {
          Ember.get(this, 'flashMessages').danger('Error: ' + adapterError.errors[0].detail);
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
