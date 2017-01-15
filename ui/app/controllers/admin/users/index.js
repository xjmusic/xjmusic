import Ember from 'ember';

export default Ember.Controller.extend({
  users: [],

  init: function() {
    this.users = this.store.findAll('user');
  }
});
