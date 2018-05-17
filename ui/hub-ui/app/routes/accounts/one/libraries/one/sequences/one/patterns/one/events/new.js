// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get } from '@ember/object';
import {hash} from 'rsvp';
import { inject as service } from '@ember/service';
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
      let pattern = this.modelFor('accounts.one.libraries.one.sequences.one.patterns.one');
      let sequence = this.modelFor('accounts.one.libraries.one.sequences.one');
      let event = this.store.createRecord('pattern-event', {
        pattern: pattern,
      });
      event.set('pattern', pattern);
      let self = this;
      let voices = this.store.query('voice', {sequenceId: sequence.get('id')})
        .catch((error) => {
          get(self, 'display').error(error);
          self.transitionTo('');
        });
      return hash({
        event: event,
        voices: voices,
      });
    } else {
      this.transitionTo('accounts.one.libraries.one.sequences.one.patterns.one.events');
    }
  },

  /**
   * Route Actions
   */
  actions: {

    createEvent() {
      let model = get(this, 'controller.model.event');
      model.save().then(
        () => {
          get(this, 'display').success('Created "' + model.get('inflection') + '" event in ' + model.get('note') + '.');
          this.transitionTo('accounts.one.libraries.one.sequences.one.patterns.one.events');
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    willTransition(transition) {
      let model = get(this, 'controller.model.event');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
        } else {
          transition.abort();
        }
      }
    },

    cancelCreateEvent() {
      let model = get(this, 'controller.model.event');
      if (model.get('hasDirtyAttributes')) {
        let confirmation = confirm("Your changes haven't saved yet. Would you like to leave this form?");
        if (confirmation) {
          model.rollbackAttributes();
          this.transitionTo('accounts.one.libraries.one.sequences.one.patterns.one.events');
        }
      }
    }

  }
});
