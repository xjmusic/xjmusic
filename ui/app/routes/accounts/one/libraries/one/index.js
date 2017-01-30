import Ember from 'ember';

export default Ember.Route.extend({

  model: function() {
    let account = this.modelFor('accounts.one');
    let library =this.modelFor('accounts.one.libraries.one');
    return Ember.RSVP.hash({
      account: account,
      library: library,
    });
  },

});
