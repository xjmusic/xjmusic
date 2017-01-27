// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  account: Ember.inject.service(),

  display: Ember.inject.service(),

  model: function() {
    let account = this.get('account').get('current');
    if (account==null) {
      this.transitionTo('library');
    }
    return this.store.createRecord('library', {
      account: account
    });
  },

  actions: {

    createLibrary(model) {
      model.save().then(() => {
        Ember.get(this, 'display').success('Created library '+model.get('name')+'.');
        this.transitionTo('library');
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
