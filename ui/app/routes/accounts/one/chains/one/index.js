import Ember from 'ember';

export default Ember.Route.extend({

  model: function() {
    let account = this.modelFor('accounts.one');
    let chain =this.modelFor('accounts.one.chains.one');
    return Ember.RSVP.hash({
      account: account,
      chain: chain,
    });
  },

});
