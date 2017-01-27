// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model(params) {
    return this.store.findRecord('library', params.library_id)
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('library');
      });
  },

  actions: {

    saveLibrary(model) {
      model.save().then(() => {
        Ember.get(this, 'display').success('Updated library ' + model.get('name') + '.');
        this.transitionTo('library');
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

    destroyLibrary(model) {
      let confirmation = confirm("Are you fucking sure? If there are Ideas or Instruments belonging to this Library, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord().then(() => {
          Ember.get(this, 'display').success('Deleted library ' + model.get('name') + '.');
          this.transitionTo('library');
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
