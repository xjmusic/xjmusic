//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {inject as service} from '@ember/service';
import {set} from '@ember/object';

import Component from '@ember/component';

/**
 * Displays Segment Choices
 */
const EditInstrumentAttributesComponent = Component.extend({

  // Whether to show the edit modal
  isEditModalVisible: false,

  // Inject: configuration service
  config: service(),

  // Inject: flash message service
  display: service(),

  // Inject: router
  router: service(),

  /**
   * Component Actions
   */
  actions: {

    setState(state) {
      this.model.set('state', state);
    },

    setType(type) {
      this.model.set('type', type);
    },

    /**
     * Set the visibility of the edit modal
     */
    show: function () {
      set(this, 'isEditModalVisible', true);
    },

    /**
     * Create a instrument
     */
    create: function () {
      let self = this;
      let instrument = this.model;
      let library = instrument.get("library");
      let account = library.get("account");
      instrument.save().then(
        () => {
          self.router.transitionTo("accounts.one.libraries.one.instruments.editor", account, library, instrument);
          self.display.success('Saved ' + instrument.get('name') + '.');
          set(this, 'isEditModalVisible', false);
        },
        (error) => {
          self.display.error(error);
        });
    },

    /**
     * Close the modal
     */
    close: function () {
      set(this, 'isEditModalVisible', false);
    },

    /**
     * Cancel creation
     */
    cancel: function () {
      set(this, 'isEditModalVisible', false);
    },

  }

});

/**
 * Usage (e.g, in Handlebars, where segment model is "mySegmentModel"):
 *
 *   {{segment-choices mySegmentModel}}
 */
EditInstrumentAttributesComponent.reopenClass({
  positionalParams: ['model']
});

export default EditInstrumentAttributesComponent;
