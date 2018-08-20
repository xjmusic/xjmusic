//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get } from '@ember/object';

import { hash } from 'rsvp';
import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*}
   */
  model() {
    let sequence = this.modelFor('accounts.one.libraries.one.sequences.one');
    return hash({
      sequence: sequence,
      memeToAdd: null,
      sequenceMemes: this.store.query('sequence-meme', {sequenceId: sequence.id}),
    });
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    destroySequenceMeme(model) {
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Removed Meme from Sequence.');
        },
        (error) => {
          get(this, 'display').error(error);
          model.rollbackAttributes();
        });
    },

    addMemeToSequence(model) {
      let sequenceMeme = this.store.createRecord('sequence-meme', {
        sequence: model.sequence,
        name: model.memeToAdd,
      });
      sequenceMeme.save().then(
        () => {
          get(this, 'display').success('Added ' + sequenceMeme.get('name') + ' to ' + model.sequence.get('name') + '.');
          this.send("sessionChanged");
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

  }


});
