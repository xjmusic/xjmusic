// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";
// import momentComputed from 'ember-moment/computeds/moment';
// import format from 'ember-moment/computeds/format';
// import locale from 'ember-moment/computeds/locale';
import moment from "moment";

export default Ember.Controller.extend({
  auth: Ember.inject.service(),

  needs: ['application'],

  timeNowUTC: '',

  init() {
    let ctrl = this;
    setInterval(function () {
      let now = new Date();
      let nowUTC = moment(
        new Date(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate(), now.getUTCHours(), now.getUTCMinutes(), now.getUTCSeconds())
      ).format("YYYY-MM-DD HH:mm:ss");
      if (nowUTC !== ctrl.get('timeNowUTC')) {
        ctrl.set('timeNowUTC', nowUTC);
        document.getElementById('clock').innerHTML = nowUTC + " Z";
      }
    }, 250);
  },

  actions: {}
});
