// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  auth: Ember.inject.service(),

  display: Ember.inject.service(),

  model: function () {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {

      let instrument = this.store.createRecord('instrument', {
        library: this.modelFor('accounts.one.libraries.one')
      });

      // resolves the user *after* closure
      this.store.findRecord('user', this.get('auth').userId).then((record) => {
        instrument.set('user', record);
      });

      return instrument;
    } else {
      this.transitionTo('accounts.one.libraries.one.instruments');
    }
  },

  actions: {

    createInstrument(model) {
      model.save().then(() => {
        Ember.get(this, 'display').success('Created instrument ' + model.get('description') + '.');
        this.transitionTo('accounts.one.libraries.one.instruments');
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
