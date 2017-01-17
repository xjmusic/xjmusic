// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({
  model: function() {
    return this.store.createRecord('account');
  },

  actions: {

    createAccount(model) {
      model.save().then(() => {
        Ember.get(this, 'flashMessages').success('Account created.');
        this.transitionTo('admin.accounts');
      }).catch((adapterError) => {
        Ember.get(this, 'flashMessages').danger('Error: ' + adapterError.errors[0].detail);
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
