// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

import {inject as service} from '@ember/service';
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
      this.set('messageBadgesModalTitle', properCase(message.get('type')) + ', Segment #' + message.get('segment').get('id'));
      this.set('messageBadgesModalBody', message.get('body'));
      this.set('messageType', (message.get('type') + '-type').toLowerCase());
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
