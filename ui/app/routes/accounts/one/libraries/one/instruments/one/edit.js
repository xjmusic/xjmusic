// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  auth: Ember.inject.service(),

  display: Ember.inject.service(),

  model() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let library = this.modelFor('accounts.one.libraries.one');
      let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
      instrument.set('library', library);
      return instrument;
    } else {
      this.transitionTo('accounts.one.libraries.one.instruments');
    }
  },

  actions: {

    saveInstrument(model) {
      model.save().then(() => {
        Ember.get(this, 'display').success('Updated instrument ' + model.get('name') + '.');
        this.transitionTo('accounts.one.libraries.one.instruments', model.get('library'));
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

    destroyInstrument(model) {
      let confirmation = confirm("Are you sure? If there are Instruments or Instruments belonging to this Instrument, deletion will fail anyway.");
      if (confirmation) {
        model.destroyRecord().then(() => {
          Ember.get(this, 'display').success('Deleted instrument ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.instruments');
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
