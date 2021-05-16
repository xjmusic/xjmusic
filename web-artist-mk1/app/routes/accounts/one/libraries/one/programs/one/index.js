/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import {get} from '@ember/object';

import {hash} from 'rsvp';
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
   * Route Model
   * @returns {*} hash
   */
  model: function () {
    let program = this.modelFor('accounts.one.libraries.one.programs.one');
    return hash({
      labUrl: `/#/programs/${program.get('id')}`,
    }, 'program');
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
      let program = this.modelFor('accounts.one.libraries.one.programs.one');
      let account = this.modelFor('accounts.one');
      let name = 'Preview of "' + program.get('name') + '" Program';
      let chain = this.store.createRecord('chain', {
        account: account,
        name: name,
        state: DRAFT,
        type: PREVIEW
      });
      chain.save().then(
        () => {
          get(self, 'display').success('Created chain ' + chain.get('name') + '.');
          self.addProgram(chain);
        },
        (error) => {
          get(self, 'display').error(error);
        });
    }
  },

  /**
   * Add Program to Chain (quick-preview, step 2)
   * @param chain
   */
  addProgram: function (chain) {
    let self = this;
    let program = this.modelFor('accounts.one.libraries.one.programs.one');
    let chainProgram = this.store.createRecord('chain-program', {
      chain: chain,
      program: program,
    });
    chainProgram.save().then(
      () => {
        get(self, 'display').success('Added ' + program.get('name') + ' to ' + chain.get('name') + '.');
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
        self.transitionTo('accounts.one.chains.one', chain);
      },
      (error) => {
        get(self, 'display').error(error);
      });
  }

});
