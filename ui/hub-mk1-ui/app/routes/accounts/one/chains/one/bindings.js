// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
import Route from '@ember/routing/route';
import {get} from '@ember/object';
import {hash, Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';

export default Route.extend({

  // Inject: configuration service
  config: service(),

  // Inject: flash message service
  display: service(),

  /**
   * Model is a promise because it depends on promised configs
   * @returns {Promise}
   */
  model() {
    return new EmberPromise((resolve, reject) => {
      this.config.getConfig().then(
        () => {
          let account = this.modelFor('accounts.one');
          let chain = this.modelFor('accounts.one.chains.one');

          let chainBindings = this.store.query('chain-binding', {chainId: chain.get('id')})
            .catch((error) => {
              get(this, 'display').error(error);
              this.transitionTo('');
            });

          resolve(hash({
            chain: chain,
            chainBindings: chainBindings,
            libraries: this.store.query('library', {accountId: account.get('id')}),
            bindingType: '',
            bindingTargetId: '',
            chainBindingTypes: this.config.chainBindingTypes,
          }, 'chain, chain bindings, chain binding types, config to add to chain'));

        },
        (error) => {
          reject('Failed to load config', error);
        }
      );
    });
  },

  //
  actions: {

    //
    sessionChanged: function () {
      this.modelFor('accounts.one.chains.one').reload();
      this.refresh();
    },

    removeBinding(model) {
      model.destroyRecord({}).then(
        () => {
          this.display.success('Removed Config from Chain.');
          this.send("sessionChanged");
        },
        (error) => {
          console.error(error);
          get(this, 'display').error(error);
        });
    },

    addBinding(model) {
      let chainBinding = this.store.createRecord('chain-binding', {
        chain: model.chain,
        type: model.bindingType,
        targetId: model.bindingTargetId
      });

      chainBinding.save().then(
        () => {
          let library;
          model.libraries.forEach(search => {
            if (search.get("id") === model.bindingTargetId)
              library = search;
          });
          get(this, 'display').success(`Added Library: ${library.get("name")}`);
          // this.transitionToRoute('chains.one.configs',model.chain);
          this.send("sessionChanged");
        },
        (error) => {
          console.error(error);
          get(this, 'display').error(error);
        });
    },

  },

});
