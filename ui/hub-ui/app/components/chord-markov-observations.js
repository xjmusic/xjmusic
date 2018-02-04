// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
import Component from '@ember/component';
// import {get, set} from '@ember/object';
// import {inject as service} from '@ember/service';

/**
 * Displays the digest-performing and result-viewing U.I.
 */
const ChordMarkovObservationsComponent = Component.extend({
});

/**
 * Example Usage (e.g, in Handlebars):
 *
 *   {{chord-markov-observations observations}}
 */
ChordMarkovObservationsComponent.reopenClass({
  positionalParams: ['observations']
});

export default ChordMarkovObservationsComponent;
