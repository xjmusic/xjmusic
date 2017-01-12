import Ember from 'ember';

export default Ember.Controller.extend({
  init: function() {
    window.location.replace("/auth/google");
  },
});
