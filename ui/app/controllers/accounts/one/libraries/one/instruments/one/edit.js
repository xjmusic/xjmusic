import Ember from 'ember';

export default Ember.Controller.extend({
  config: Ember.inject.service(),

  actions: {

    selectInstrumentType(type) {
      Ember.get(this, 'model').set('type', type);
    },

  }

});
