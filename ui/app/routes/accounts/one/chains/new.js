// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  config: Ember.inject.service(),

  display: Ember.inject.service(),

  model: function() {
    let account = this.modelFor('accounts.one');
    return this.store.createRecord('chain', {
      account: account,
      state:  Ember.get(this, 'config').chainStates[0]
    });
  },

  actions: {

    createChain(model) {
      model.save().then(() => {
        Ember.get(this, 'display').success('Created chain '+model.get('name')+'.');
        this.transitionTo('accounts.one.chains');
      }).catch((error) => {
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
