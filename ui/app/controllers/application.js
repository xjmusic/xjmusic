// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Controller.extend({
  auth: Ember.inject.service(),

  needs: ['application']

});
