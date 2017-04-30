// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({

  /**
   * Route Model
   * @returns {*}
   */
  model: function() {
    let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
    let audio = this.modelFor('accounts.one.libraries.one.instruments.one.audios.one');
    return Ember.RSVP.hash({
      instrument: instrument,
      audio: audio,
    });
  },

  /**
   * Headline
   */
  afterModel(model) {
    Ember.set(this, 'routeHeadline', {
      title: model.audio.get('name'),
      entity: {
        name: 'Audio',
        id: model.audio.get('id')
      }
    });
  },

});
