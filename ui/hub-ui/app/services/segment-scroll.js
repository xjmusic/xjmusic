//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Service from '@ember/service';

export default Service.extend({

  /**
   Scroll to a segment, if visible
   * @param segment model to scroll to
   */
  scrollTo: function (segment) {
    let segmentId = "segment-" + segment.get('id');
    let elSegment = document.getElementById(segmentId);
    if (elSegment && elSegment.length)
      elSegment.scrollIntoView();
  }

});
