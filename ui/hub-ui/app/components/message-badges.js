// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import $ from 'jquery';

import { inject as service } from '@ember/service';
import Component from '@ember/component';

/**
 * Displays a Message badges U.I., e.g. Link Messages
 */
const MessageBadgesComponent = Component.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Component Actions
   */
  actions: {

    showMessage(message) {
      $('#messageBadgesModalTitle').html(properCase(message.get('type')) + ', Link #' + message.get('link').get('id') );
      $('#messageBadgesModalBody').html(message.get('body'));
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
 * Usage (e.g, in Handlebars, where link model is "myLinkModel"):
 *
 *   {{message-badges myLinkModel}}
 */
MessageBadgesComponent.reopenClass({
  positionalParams: ['model']
});

export default MessageBadgesComponent;
