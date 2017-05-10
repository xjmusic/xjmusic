// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from 'ember';

export default Ember.Route.extend({
  init() {
    let element = document.getElementById("static-content");
    element.parentNode.removeChild(element);
  }
});
