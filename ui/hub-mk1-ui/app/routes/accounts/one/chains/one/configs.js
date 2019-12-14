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
          let chain = this.modelFor('accounts.one.chains.one');

          let chainConfigs = this.store.query('chain-config', {chainId: chain.get('id')})
            .catch((error) => {
              get(this, 'display').error(error);
              this.transitionTo('');
            });

          resolve(hash({
            chain: chain,
            chainConfigs: chainConfigs,
            configType: '',
            configValue: '',
            chainConfigTypes: this.config.chainConfigTypes,
          }, 'chain, chain configs, chain config types, config to add to chain'));

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

    removeConfig(model) {
      model.destroyRecord({}).then(
        () => {
          this.display.success('Removed Config from Chain.');
          this.send("sessionChanged");
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

    addConfig(model) {
      let chainConfig = this.store.createRecord('chain-config', {
        chain: model.chain,
        type: model.configType,
        value: model.configValue
      });

      chainConfig.save().then(
        (saved) => {
          get(this, 'display').success('Added ' + saved.get('type') + '=' + saved.get('value'));
          // this.transitionToRoute('chains.one.configs',model.chain);
          this.send("sessionChanged");
        },
        (error) => {
          get(this, 'display').error(error);
        });
    },

  },

});
