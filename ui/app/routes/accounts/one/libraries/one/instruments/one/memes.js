// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model() {
    let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
    return Ember.RSVP.hash({
      instrument: instrument,
      memeToAdd: null,
      instrumentMemes: this.store.query('instrument-meme', { instrumentId: instrument.id }),
    });
  },

  actions: {

    sessionChanged: function() {
      this.refresh();
    },

    destroyInstrumentMeme(model) {
      model.destroyRecord().then(() => {
        Ember.get(this, 'display').success('Removed Meme from Instrument.');
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

    addMemeToInstrument(model) {
      let instrumentMeme = this.store.createRecord('instrument-meme', {
        instrument: model.instrument,
        name: model.memeToAdd,
      });
      instrumentMeme.save().then(() => {
        Ember.get(this, 'display').success('Added ' + instrumentMeme.get('name') + ' to ' + model.instrument.get('description') + '.');
        this.send("sessionChanged");
      }).catch((error) => {
        Ember.get(this, 'display').error(error);
      });
    },

  }


});
