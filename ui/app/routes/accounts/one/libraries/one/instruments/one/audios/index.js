// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  display: Ember.inject.service(),

  model: function () {
    let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
    let audios = this.store.query('audio', {instrumentId: instrument.get('id')})
      .catch((error) => {
        Ember.get(this, 'display').error(error);
        this.transitionTo('');
      });
    return Ember.RSVP.hash({
      instrument: instrument,
      audios: audios,
    });
  },

  actions: {

    editAudio(audio) {
      this.transitionTo('accounts.one.libraries.one.instruments.one.audios.one', audio);
    },

  }
});
