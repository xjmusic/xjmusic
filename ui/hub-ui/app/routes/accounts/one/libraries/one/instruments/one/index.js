// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
import { get } from '@ember/object';

import { hash } from 'rsvp';
import { inject as service } from '@ember/service';
import Route from '@ember/routing/route';

let DRAFT = "draft";
let READY = "ready";
let FABRICATE = "fabricate";
let PREVIEW = "preview";

export default Route.extend({

  // Inject: flash message service
  display: service(),

  /**
   * Route Model
   * @returns {*}
   */
  model: function () {
    let library = this.modelFor('accounts.one.libraries.one');
    let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
    return hash({
      library: library,
      instrument: instrument,
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
      let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
      let account = this.modelFor('accounts.one');
      let name = 'Preview of "' + instrument.get('description') + '" Instrument';
      let chain = this.store.createRecord('chain', {
        account: account,
        name: name,
        state: DRAFT,
        type: PREVIEW
      });
      chain.save().then(
        () => {
          get(self, 'display').success('Created chain ' + chain.get('name') + '.');
          self.addInstrument(chain);
        },
        (error) => {
          get(self, 'display').error(error);
        });
    }
  },

  /**
   * Add Instrument to Chain (quick-preview, step 2)
   * @param chain
   */
  addInstrument: function (chain) {
    let self = this;
    let instrument = this.modelFor('accounts.one.libraries.one.instruments.one');
    let chainInstrument = this.store.createRecord('chain-instrument', {
      chain: chain,
      instrument: instrument,
    });
    chainInstrument.save().then(
      () => {
        get(self, 'display').success('Added ' + instrument.get('description') + ' to ' + chain.get('name') + '.');
        self.addLibrary(chain);
      },
      (error) => {
        get(self, 'display').error(error);
      });
  },

  /**
   * Add Library to Chain (quick-preview, step 3)
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
        self.updateToReady(chain);
      },
      (error) => {
        get(self, 'display').error(error);
      });
  },

  /**
   * Update Chain to Ready-state (quick-preview, step 4)
   * @param chain
   */
  updateToReady: function (chain) {
    let self = this;
    chain.set('state', READY);
    chain.save().then(
      () => {
        get(self, 'display').success('Advanced chain state to ' + READY + '.');
        self.updateToFabricate(chain);
      },
      (error) => {
        get(self, 'display').error(error);
      });
  },

  /**
   * Update Chain to Fabricate-state (quick-preview, step 5)
   * @param chain
   */
  updateToFabricate: function (chain) {
    let self = this;
    chain.set('state', FABRICATE);
    chain.save().then(
      () => {
        get(self, 'display').success('Advanced chain state to ' + FABRICATE + '.');
        self.transitionTo('accounts.one.chains.one.links', chain);
      },
      (error) => {
        get(self, 'display').error(error);
      });
  }

});
