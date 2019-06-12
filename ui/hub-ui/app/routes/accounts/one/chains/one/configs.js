//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Route from '@ember/routing/route';
import {get} from '@ember/object';
import {hash, Promise as EmberPromise} from 'rsvp';
import {inject as service} from '@ember/service';
import {v4 as uuid} from "ember-uuid";

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
      let self = this;
      this.config.getConfig().then(
        () => {
          resolve(hash({
            chain: self.modelFor('accounts.one.chains.one'),
            configType: '',
            configValue: '',
            chainConfigTypes: self.config.chainConfigTypes,
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

    /**
     * Remove a chainConfig record
     * @param chainConfig record to remove
     */
    removeConfig(chainConfig) {
      let self = this;
      let chain = this.modelFor('accounts.one.chains.one');
      this.store.deleteRecord(chainConfig);
      chain.save().then(
        () => {
          self.display.success('Removed Config from Chain.');
          self.send("sessionChanged");
        },
        (error) => {
          get(self, 'display').error(error);
        });
    },

    /**
     * Add a new Config
     * @param type of new config
     * @param value of new config
     */
    addConfig(type, value) {
      let self = this;
      let chain = this.modelFor('accounts.one.chains.one');
      let addedConfig = this.store.push({
        data: {
          id: uuid(),
          type: 'chain-config',
          attributes: {
            type: type,
            value: value
          },
          relationships: {
            chain: {
              data: {
                id: chain.get('id'),
                type: 'chain'
              }
            }
          }
        }
      });
      chain.save().then(
        () => {
          get(self, 'display').success(`Added ${addedConfig.get('type')}="${addedConfig.get('value')}"`);
        },
        (error) => {
          chain.rollbackAttributes();
          chain.reload().then(() => {
            get(self, 'display').error(error);
          });
        });
    },

  },

});
