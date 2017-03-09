// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({
  renderTemplate: function() {
    window.location.replace("/auth/no");
  },
  breadCrumb: null
});
