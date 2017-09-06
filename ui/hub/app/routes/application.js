// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from "ember";

export default Ember.Route.extend({
  init() {
    this.deleteStaticContent();
  },

  /**
   [#155] Create actual static content on default index.html that gets deleted by Ember init, in order to build search engine value at https://xj.io
   */
  deleteStaticContent() {
    let element = document.getElementById("static-content");
    element.parentNode.removeChild(element);
  },

  actions: {
    sessionChanged: function () {
      this.refresh();
    }
  }
});
