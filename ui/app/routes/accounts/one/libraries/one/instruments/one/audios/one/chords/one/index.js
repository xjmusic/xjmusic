// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    let chord = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one.chords.one');
    return Ember.RSVP.hash({
      audio: audio,
      chord: chord,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.chord.getTitle(),
      entity: {
        name: 'Chord',
        id: model.chord.get('id')
      }
    });
  },

});
