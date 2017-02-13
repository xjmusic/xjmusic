// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({

  auth: Ember.inject.service(),

  display: Ember.inject.service(),

  model() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
      let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
      audio.set('instrument', instrument);
      return audio;
    } else {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios');
    }
  },

  actions: {

    saveAudio(model) {
      model.save().then(() => {
        Ember.get(this, 'display').success('Updated audio "' + model.get('name') + '".');
        this.transitionTo('accounts.one.libraries.one.instruments.one.audios');
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

    destroyAudio(model) {
      model.destroyRecord().then(() => {
        Ember.get(this, 'display').success('Deleted audio "' + model.get('name') + '".');
        this.transitionTo('accounts.one.libraries.one.instruments.one.audios');
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
