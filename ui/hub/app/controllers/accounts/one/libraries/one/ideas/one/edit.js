import Ember from 'ember';

export default Ember.Controller.extend({
  config: Ember.inject.service(),

  actions: {

    selectIdeaType(type) {
      Ember.get(this, 'model').set('type', type);
    },

  }

});
