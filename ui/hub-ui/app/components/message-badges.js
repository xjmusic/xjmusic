//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import $ from 'jquery';

import { inject as service } from '@ember/service';
import Component from '@ember/component';

/**
 * Displays a Message badges U.I., e.g. Segment Messages
 */
const MessageBadgesComponent = Component.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Component Actions
   */
  actions: {

    showMessage(message) {
      $('#messageBadgesModalTitle').html(properCase(message.get('type')) + ', Segment #' + message.get('segment').get('id'));
      $('#messageBadgesModalBody').html(message.get('body'));

      let msgClass = (message.get('type') + '-type').toLowerCase();
      this.$('.modal-header').addClass(msgClass);
      console.log("add your shit", this.$('.modal-header'), msgClass, this.$('.modal-header').hasClass(msgClass));
    }

  },

});

/**
 First letter upper, rest lowercase
 * @param raw
 * @returns {string}
 */
function properCase(raw) {
  return raw.substr(0, 1).toUpperCase() + raw.substr(1).toLowerCase();
}

/**
 * Usage (e.g, in Handlebars, where segment model is "mySegmentModel"):
 *
 *   {{message-badges mySegmentModel}}
 */
MessageBadgesComponent.reopenClass({
  positionalParams: ['model']
});

export default MessageBadgesComponent;
