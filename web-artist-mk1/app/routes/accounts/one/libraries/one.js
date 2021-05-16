/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {get} from '@ember/object';

import {inject as service} from '@ember/service';
import Route from '@ember/routing/route';

let DRAFT = "draft";
let READY = "ready";
let FABRICATE = "fabricate";
let PREVIEW = "preview";

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route model
   * @param params
   * @returns {Promise.<T>}
   */
  model(params) {
    let self = this;
    return this.store.findRecord('library', params['library_id'], {reload: true})
      .catch((error) => {
        get(self, 'display').error(error);
        self.transitionTo('accounts.one.libraries');
      });
  },

  /**
   * Route Actions
   */
  actions: {

    /**
     * Create a quick-preview chain (step 1)
     */
    quickPreview() {
      let self = this;
      let library = this.modelFor('accounts.one.libraries.one');
      let account = this.modelFor('accounts.one');
      let name = 'Preview of "' + library.get('name') + '" Library';
      let chain = this.store.createRecord('chain', {
        account: account,
        name: name,
        state: DRAFT,
        type: PREVIEW
      });
      chain.save().then(
        () => {
          get(self, 'display').success('Created chain ' + chain.get('name') + '.');
          self.send('addLibrary', chain);
        },
        (error) => {
          get(self, 'display').error(error);
        });
    },


    /**
     * Add Library to Chain (quick-preview, step 2)
     * @param chain
     */
    addLibrary: function (chain) {
      let self = this;
      let library = this.modelFor('accounts.one.libraries.one');
      let chainLibrary = this.store.createRecord('chain-library', {
        chain: chain,
        library: library,
      });
      chainLibrary.save().then(
        () => {
          get(self, 'display').success('Added ' + library.get('name') + ' to ' + chain.get('name') + '.');
          self.send('updateToReady', chain);
        },
        (error) => {
          get(self, 'display').error(error);
        });
    },

    /**
     * Update Chain to Ready-state (quick-preview, step 3)
     * @param chain
     */
    updateToReady: function (chain) {
      let self = this;
      chain.set('state', READY);
      chain.save().then(
        () => {
          get(self, 'display').success('Advanced chain state to ' + READY + '.');
          self.send('updateToFabricate', chain);
        },
        (error) => {
          get(self, 'display').error(error);
        });
    },

    /**
     * Update Chain to Fabricate-state (quick-preview, step 4)
     * @param chain
     */
    updateToFabricate: function (chain) {
      let self = this;
      chain.set('state', FABRICATE);
      chain.save().then(
        () => {
          get(self, 'display').success('Advanced chain state to ' + FABRICATE + '.');
          self.transitionTo('accounts.one.chains.one', chain);
        },
        (error) => {
          get(self, 'display').error(error);
        });
    }

  }

});
