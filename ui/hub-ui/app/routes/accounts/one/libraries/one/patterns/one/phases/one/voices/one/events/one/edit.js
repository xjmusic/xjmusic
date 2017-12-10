// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import {get} from '@ember/object';

import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: authentication service
  auth: service(),

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*|DS.Model|Promise}
   */
  model() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let voice = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one.voices.one');
      let event = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one.voices.one.events.one');
      event.set('voice', voice);
      return event;
    } else {
      this.transitionTo('accounts.one.libraries.one.patterns.one.phases.one.voices.one.events');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    saveEvent(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Updated event "' + model.get('inflection') + '" event in ' + model.get('note') + '.');
          history.back();
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    destroyEvent(model) {
      let voice = model.get('voice');
      let phase = voice.get('phase');
      let pattern = phase.get('pattern');
      let library = pattern.get('library');
      let account = library.get('account');
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Deleted event "' + model.get('inflection') + '" event in ' + model.get('note') + '.');
          this.transitionTo('accounts.one.libraries.one.patterns.one.phases.one.voices.one.events', account, library, pattern, phase, voice);
        },
        (error) => {
          get(this, 'display').error(error);
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
