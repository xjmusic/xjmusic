// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model(params) {
    return this.store.findRecord('user', params.user_id).catch((error)=>{
      Ember.get(this, 'display').error(error);
      this.transitionTo('users');
    });
  },

  afterModel(model) {
    Ember.set(this, 'breadCrumb', model);
  },

  actions: {

    saveUser(model) {
      model.save().then(() => {
        Ember.get(this, 'display').success('Updated user '+model.get('name')+'.');
        this.transitionTo('users');
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
    },

  },

});
