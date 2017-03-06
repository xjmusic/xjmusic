import Ember from 'ember';

export default Ember.Service.extend({

  /**
   * On app start, contact the backend to get some constant values
   */
  init() {
    this._super(...arguments);
    let config = this;

    Ember.$.ajax({
      url: '/api/1/config'
    }).then(function (resolved) {
      if (resolved.hasOwnProperty('config')) {
        let recvConfig = resolved['config'];
        for (let key in recvConfig) {
          if (recvConfig.hasOwnProperty(key)) {
            let val = recvConfig[key];
            console.log("Configuration[ " + key + " ]=> " + val);
            Ember.set(config, key, val);
          }
        }
      }

    }, function (rejected) {
      Ember.get(config, 'display').error('Failed to upload Audio File.');
      console.log(rejected);
    });
  },

  /**
   * Flash messaging
   */
  display: Ember.inject.service(),

  chainStates: [],

  baseUrl: "",

  apiBaseUrl: "",

  audioBaseUrl: ""

});
