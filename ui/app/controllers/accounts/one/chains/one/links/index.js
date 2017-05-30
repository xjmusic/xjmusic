// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Controller.extend({

  // from offset, to read many links
  queryParams: [
    'fromOffset'
  ],

  // offset is null by default (read from latest)
  fromOffset: null

});
