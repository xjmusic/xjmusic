// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
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
   * @returns {*|DS.Model}
   */
  model() {
    let auth = this.get('auth');
    if (auth.isArtist || auth.isAdmin) {
      let phase = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one');
      let chord = this.modelFor('accounts.one.libraries.one.patterns.one.phases.one.chords.one');
      chord.set('phase', phase);
      return chord;
    } else {
      this.transitionTo('accounts.one.libraries.one.patterns.one.phases.one.chords');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    saveChord(model) {
      model.save().then(
        () => {
          get(this, 'display').success('Updated chord ' + model.get('name') + '.');
          history.back();
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    destroyChord(model) {
      let phase = model.get('phase');
      let pattern = phase.get('pattern');
      let library = pattern.get('library');
      let account = library.get('account');
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Deleted chord ' + model.get('name') + '.');
          this.transitionTo('accounts.one.libraries.one.patterns.one.phases.one.chords', account, library, pattern, phase);
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
