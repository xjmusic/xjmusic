//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
import {inject as service} from '@ember/service';
import {set} from '@ember/object';

import Component from '@ember/component';

/**
 * Displays Segment Choices
 */
const EditProgramAttributesComponent = Component.extend({

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
     * Create a program
     */
    create: function () {
      let self = this;
      let program = this.model;
      let library = program.get("library");
      let account = library.get("account");
      program.save().then(
        () => {
          self.router.transitionTo("accounts.one.libraries.one.programs.editor", account, library, program);
          self.display.success('Saved ' + program.get('name') + '.');
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
EditProgramAttributesComponent.reopenClass({
  positionalParams: ['model']
});

export default EditProgramAttributesComponent;
