// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  // Inject: authentication service
  auth: Ember.inject.service(),

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Route Model
   * @returns {*|DS.Model}
   */
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

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: 'Edit ' + model.get('name'),
      entity: {
        name: 'Audio',
        id: model.get('id')
      }
    });
  },

  /**
   * Route Actions
   */
  actions: {

    saveAudio(model) {
      model.save().then(
        () => {
          Ember.get(this, 'display').success('Updated audio "' + model.get('name') + '".');
          this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one');
        },
        (error) => {
          Ember.get(this, 'display').error(error);
        });
    },

    destroyAudio(model) {
      model.destroyRecord({}).then(
        () => {
          Ember.get(this, 'display').success('Deleted audio "' + model.get('name') + '".');
          this.transitionTo('accounts.one.libraries.one.instruments.one.audios');
        },
        (error) => {
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
