import Ember from 'ember';

export default Ember.Route.extend({

  model: function() {
    let library = this.modelFor('accounts.one.libraries.one');
    let idea =this.modelFor('accounts.one.libraries.one.ideas.one');
    return Ember.RSVP.hash({
      library: library,
      idea: idea,
    });
  },

});
