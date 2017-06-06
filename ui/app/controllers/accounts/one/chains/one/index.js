// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Controller.extend({

  /**
   Inject: chain-link player service, implemented directly by the template in order to
   [#279] Now-playing Chain or Link appears highlighted
   */
  play: Ember.inject.service(),

});
