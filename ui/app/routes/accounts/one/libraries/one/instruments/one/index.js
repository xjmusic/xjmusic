import Ember from 'ember';

export default Ember.Route.extend({

  model: function() {
    let library = this.modelFor('accounts.one.libraries.one');
    let instrument =this.modelFor('accounts.one.libraries.one.instruments.one');
    return Ember.RSVP.hash({
      library: library,
      instrument: instrument,
    });
  },

});
