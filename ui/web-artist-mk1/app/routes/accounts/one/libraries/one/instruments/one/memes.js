/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {get} from '@ember/object';

import {hash} from 'rsvp';
import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*} hash
   */
  model() {
    let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
    return hash({
      instrument: instrument,
      memeToAdd: null,
      instrumentMemes: this.store.query('instrument-meme', {instrumentId: instrument.id}),
    }, 'instrument, instrument memes, meme to add to instrument');
  },

  /**
   * Route Actions
   */
  actions: {

    sessionChanged: function () {
      this.refresh();
    },

    destroyInstrumentMeme(model) {
      model.destroyRecord({}).then(
        () => {
          get(this, 'display').success('Removed Meme from Instrument.');
        },
        (error) => {
          get(this, 'display').error(error);
          model.rollbackAttributes();
        });
    },

    addMemeToInstrument(model) {
      let instrumentMeme = this.store.createRecord('instrument-meme', {
        instrument: model.instrument,
        name: model.memeToAdd,
      });
      instrumentMeme.save().then(
        () => {
          get(this, 'display').success('Added ' + instrumentMeme.get('name') + ' to ' + model.instrument.get('name') + '.');
          this.send("sessionChanged");
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

  }


});
