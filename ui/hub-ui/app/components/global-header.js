//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.

import {inject as service} from '@ember/service';
import Component from '@ember/component';
import Moment from "moment";

export default Component.extend({
  auth: service(),

  needs: ['application'],

  timeNowUTC: '',

  didRender() {
    this.setupClock();
    this.setupNavbarCollapse();
  },

  setupClock() {
    let ctrl = this;
    setInterval(function () {
      let now = new Date();
      let nowUTC = new Moment(
        new Date(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate(), now.getUTCHours(), now.getUTCMinutes(), now.getUTCSeconds())
      ).format("YYYY-MM-DDTHH:mm:ss");
      if (nowUTC !== ctrl.get('timeNowUTC')) {
        ctrl.set('timeNowUTC', nowUTC);
        let clock = document.getElementById('clock');
        clock.innerHTML = nowUTC + "Z";
        clock.onclick = function () {
          let inputToCopy = document.createElement("input");
          document.body.appendChild(inputToCopy);
          inputToCopy.value = nowUTC + "Z";
          inputToCopy.select();
          document.execCommand('copy');
          document.body.removeChild(inputToCopy);
        };
      }
    }, 250);
  },

  /**
   [#150279494] On mobile, open menu and make selection, expect menu to close
   */
  setupNavbarCollapse() {
    /*
      TODO [#150279494] On mobile, open menu and make selection, expect menu to close

        let self = this;
        (".navbar-nav li a").click(function (event) {
          // check if window is small enough so dropdown is created
          let dropdown = (event.target).hasClass('dropdown-toggle');
          if (dropdown) {
            // don't collapse on dropdown press
            return;
          }
          let toggle = (".navbar-toggler").is(":visible");
          if (toggle) {
            (".navbar-collapse").collapse('hide');
          }
        });
    */
  }

});


