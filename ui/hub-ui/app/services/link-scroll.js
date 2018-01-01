// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Service from '@ember/service';
import $ from 'jquery';

// Animation time
// Not optimal: requires knowledge that anything using this class should do so > this interval, in order to avoid collision
const SCROLL_TO_LINK_ANIMATION_SECONDS = 0.5;

// Ratio of available margin that will be used to fill *above* the target link
const MARGIN_TOP_RATIO = 0.8;

export default Service.extend({

  /**
   Scroll to a link, if visible
   * @param link model to scroll to
   * @param doAnimation boolean, default true
   */
  scrollTo: function (link, doAnimation) {
    // not explicitly false, defaults to true
    if (!doAnimation && doAnimation !== false) {
      doAnimation = true;
    }

    let linkId = "link-" + link.get('id');
    let elLink = $("#" + linkId);
    if (!elLink.length) {
      return;
    }

    let navHeight = $("#navigation").outerHeight();
    let verticalMargin = ($(window).innerHeight() - elLink.outerHeight() + navHeight) * MARGIN_TOP_RATIO;
    let target = $('html, body');
    target.finish();
    if (doAnimation) {
      target.animate({
        scrollTop: elLink.offset().top - verticalMargin
      }, SCROLL_TO_LINK_ANIMATION_SECONDS * MILLIS_PER_SECOND);
    } else {
      target.scrollTop(elLink.offset().top - verticalMargin);
    }
  }

});

// Math globals
const MILLIS_PER_SECOND = 1000;
