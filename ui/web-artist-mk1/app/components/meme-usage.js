/*
 * Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
 */
import Component from '@ember/component';
// import { get, set } from '@ember/object';
import {inject as service} from '@ember/service';

/**
 * Displays the digest-performing and result-viewing U.I.
 */
const MemeUsageComponent = Component.extend({

  // Inject: flash message service
  display: service(),

  /**
   Component will render
   */
  willRender() {

  },


});

/**
 * Example Usage (e.g, in Handlebars):
 *
 *   {{meme-usage name usage}}
 */
MemeUsageComponent.reopenClass({
  positionalParams: ['name', 'model']
});

export default MemeUsageComponent;
