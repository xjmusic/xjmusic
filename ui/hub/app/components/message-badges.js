// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
import Ember from "ember";

/**
 * Displays a Message badges U.I., e.g. Link Messages
 */
const MessageBadgesComponent = Ember.Component.extend({

  // Inject: flash message service
  display: Ember.inject.service(),

  /**
   * Component Actions
   */
  actions: {

    showMessage(message) {
      Ember.$('#messageBadgesModalTitle').html(properCase(message.get('type')) + ', Link #' + message.get('link').get('id') );
      Ember.$('#messageBadgesModalBody').html(message.get('body'));
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
