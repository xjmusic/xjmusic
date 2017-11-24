// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Route from '@ember/routing/route';

export default Route.extend({
  renderTemplate: function() {
    window.location.replace("/auth/no");
  },
  breadCrumb: null
});
